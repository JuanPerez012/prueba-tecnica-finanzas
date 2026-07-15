package prueba.finanzas.service.producto;

import prueba.finanzas.entity.Cliente;
import prueba.finanzas.entity.Producto;
import prueba.finanzas.enums.EstadoCuenta;
import prueba.finanzas.enums.TipoCuenta;
import prueba.finanzas.repository.ClienteRepository;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.dto.producto.ProductoCreateRequest;
import prueba.finanzas.dto.producto.ProductoResponse;
import prueba.finanzas.dto.producto.ProductoUpdateRequest;
import prueba.finanzas.mapper.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prueba.finanzas.repository.ProductoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final GeneradorNumeroCuenta generadorNumeroCuenta;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional
    public ProductoResponse crear(ProductoCreateRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un cliente con id: " + request.getClienteId()
            ));

        BigDecimal saldoInicial = request.getSaldoInicial() != null ? request.getSaldoInicial() : BigDecimal.ZERO;
        validarSaldoParaTipoCuenta(request.getTipoCuenta(), saldoInicial);

        Producto producto = Producto.builder()
            .tipoCuenta(request.getTipoCuenta())
            .numeroCuenta(generadorNumeroCuenta.generar(request.getTipoCuenta()))
            .estado(EstadoCuenta.ACTIVA)
            .saldo(saldoInicial)
            .exentaGmf(Boolean.TRUE.equals(request.getExentaGmf()))
            .cliente(cliente)
            .build();

        Producto guardado = productoRepository.save(producto);
        return productoMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return productoMapper.toResponse(buscarProductoOLanzarExcepcion(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll().stream()
            .map(productoMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("No existe un cliente con id: " + clienteId);
        }
        return productoRepository.findByClienteId(clienteId).stream()
            .map(productoMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoUpdateRequest request) {
        Producto producto = buscarProductoOLanzarExcepcion(id);

        if (producto.getEstado() == EstadoCuenta.CANCELADA) {
            throw new BusinessRuleException("La cuenta " + producto.getNumeroCuenta() + " está cancelada y no puede modificarse");
        }

        if (request.getEstado() == EstadoCuenta.CANCELADA && producto.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("Solo se pueden cancelar cuentas con saldo en $0");
        }

        producto.setEstado(request.getEstado());
        producto.setExentaGmf(Boolean.TRUE.equals(request.getExentaGmf()));
        producto.setFechaModificacion(LocalDateTime.now());

        Producto actualizado = productoRepository.save(producto);
        return productoMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarProductoOLanzarExcepcion(id);

        if (producto.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException(
                "No se puede eliminar la cuenta " + producto.getNumeroCuenta() + " porque su saldo no está en $0"
            );
        }

        productoRepository.delete(producto);
    }

    private Producto buscarProductoOLanzarExcepcion(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    private void validarSaldoParaTipoCuenta(TipoCuenta tipoCuenta, BigDecimal saldo) {
        if (tipoCuenta == TipoCuenta.AHORROS && saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Una cuenta de ahorros no puede tener saldo menor a $0");
        }
    }
}
