package prueba.finanzas.service;

import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.dto.transaccion.TransaccionResponse;
import prueba.finanzas.entity.Movimiento;
import prueba.finanzas.entity.Producto;
import prueba.finanzas.entity.Transaccion;
import prueba.finanzas.enums.EstadoCuenta;
import prueba.finanzas.enums.TipoMovimiento;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.mapper.TransaccionMapper;
import prueba.finanzas.repository.ProductoRepository;
import prueba.finanzas.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final ProductoRepository productoRepository;
    private final TransaccionMapper transaccionMapper;

    @Override
    @Transactional
    public TransaccionResponse crear(TransaccionCreateRequest request) {
        return switch (request.getTipoTransaccion()) {
            case CONSIGNACION -> procesarConsignacion(request);
            case RETIRO -> procesarRetiro(request);
            case TRANSFERENCIA -> procesarTransferencia(request);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionResponse obtenerPorId(Long id) {
        return transaccionMapper.toResponse(buscarTransaccionOLanzarExcepcion(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> obtenerTodas() {
        return transaccionRepository.findAll().stream()
            .map(transaccionMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> obtenerPorProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("No existe un producto con id: " + productoId);
        }
        return transaccionRepository
            .findByProductoOrigenIdOrProductoDestinoIdOrderByFechaCreacionDesc(productoId, productoId)
            .stream()
            .map(transaccionMapper::toResponse)
            .toList();
    }

    private TransaccionResponse procesarConsignacion(TransaccionCreateRequest request) {
        if (request.getProductoDestinoId() == null) {
            throw new BusinessRuleException("La consignación requiere una cuenta destino (productoDestinoId)");
        }
        if (request.getProductoOrigenId() != null) {
            throw new BusinessRuleException("La consignación no debe indicar cuenta origen");
        }

        Producto destino = buscarProductoOLanzarExcepcion(request.getProductoDestinoId());
        validarCuentaActiva(destino);

        BigDecimal saldoResultante = acreditar(destino, request.getMonto());
        productoRepository.save(destino);

        Transaccion transaccion = crearCabecera(request, null, destino);
        transaccion.agregarMovimiento(nuevoMovimiento(destino, TipoMovimiento.CREDITO, request.getMonto(), saldoResultante));

        return transaccionMapper.toResponse(transaccionRepository.save(transaccion));
    }

    private TransaccionResponse procesarRetiro(TransaccionCreateRequest request) {
        if (request.getProductoOrigenId() == null) {
            throw new BusinessRuleException("El retiro requiere una cuenta origen (productoOrigenId)");
        }
        if (request.getProductoDestinoId() != null) {
            throw new BusinessRuleException("El retiro no debe indicar cuenta destino");
        }

        Producto origen = buscarProductoOLanzarExcepcion(request.getProductoOrigenId());
        validarCuentaActiva(origen);

        BigDecimal saldoResultante = debitar(origen, request.getMonto());
        productoRepository.save(origen);

        Transaccion transaccion = crearCabecera(request, origen, null);
        transaccion.agregarMovimiento(nuevoMovimiento(origen, TipoMovimiento.DEBITO, request.getMonto(), saldoResultante));

        return transaccionMapper.toResponse(transaccionRepository.save(transaccion));
    }

    private TransaccionResponse procesarTransferencia(TransaccionCreateRequest request) {
        if (request.getProductoOrigenId() == null || request.getProductoDestinoId() == null) {
            throw new BusinessRuleException(
                "La transferencia requiere cuenta origen (productoOrigenId) y cuenta destino (productoDestinoId)"
            );
        }
        if (request.getProductoOrigenId().equals(request.getProductoDestinoId())) {
            throw new BusinessRuleException("La cuenta origen y la cuenta destino no pueden ser la misma");
        }

        Producto origen = buscarProductoOLanzarExcepcion(request.getProductoOrigenId());
        Producto destino = buscarProductoOLanzarExcepcion(request.getProductoDestinoId());
        validarCuentaActiva(origen);
        validarCuentaActiva(destino);

        BigDecimal saldoResultanteOrigen = debitar(origen, request.getMonto());
        BigDecimal saldoResultanteDestino = acreditar(destino, request.getMonto());
        productoRepository.save(origen);
        productoRepository.save(destino);

        Transaccion transaccion = crearCabecera(request, origen, destino);
        transaccion.agregarMovimiento(nuevoMovimiento(origen, TipoMovimiento.DEBITO, request.getMonto(), saldoResultanteOrigen));
        transaccion.agregarMovimiento(nuevoMovimiento(destino, TipoMovimiento.CREDITO, request.getMonto(), saldoResultanteDestino));

        return transaccionMapper.toResponse(transaccionRepository.save(transaccion));
    }

    private BigDecimal debitar(Producto producto, BigDecimal monto) {
        BigDecimal saldoResultante = producto.getSaldo().subtract(monto);
        if (saldoResultante.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(
                "Saldo insuficiente en la cuenta " + producto.getNumeroCuenta() + " para realizar la operación"
            );
        }
        producto.setSaldo(saldoResultante);
        return saldoResultante;
    }

    private BigDecimal acreditar(Producto producto, BigDecimal monto) {
        BigDecimal saldoResultante = producto.getSaldo().add(monto);
        producto.setSaldo(saldoResultante);
        return saldoResultante;
    }

    private void validarCuentaActiva(Producto producto) {
        if (producto.getEstado() != EstadoCuenta.ACTIVA) {
            throw new BusinessRuleException(
                "La cuenta " + producto.getNumeroCuenta() + " no está activa y no puede operar transacciones"
            );
        }
    }

    private Transaccion crearCabecera(TransaccionCreateRequest request, Producto origen, Producto destino) {
        return Transaccion.builder()
            .tipoTransaccion(request.getTipoTransaccion())
            .monto(request.getMonto())
            .descripcion(request.getDescripcion())
            .productoOrigen(origen)
            .productoDestino(destino)
            .build();
    }

    private Movimiento nuevoMovimiento(Producto producto, TipoMovimiento tipo, BigDecimal monto, BigDecimal saldoResultante) {
        return Movimiento.builder()
            .producto(producto)
            .tipoMovimiento(tipo)
            .monto(monto)
            .saldoResultante(saldoResultante)
            .build();
    }

    private Producto buscarProductoOLanzarExcepcion(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    private Transaccion buscarTransaccionOLanzarExcepcion(Long id) {
        return transaccionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con id: " + id));
    }
}
