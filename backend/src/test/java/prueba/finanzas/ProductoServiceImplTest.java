package prueba.finanzas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prueba.finanzas.dto.producto.ProductoCreateRequest;
import prueba.finanzas.dto.producto.ProductoResponse;
import prueba.finanzas.dto.producto.ProductoUpdateRequest;
import prueba.finanzas.entity.Cliente;
import prueba.finanzas.entity.Producto;
import prueba.finanzas.enums.EstadoCuenta;
import prueba.finanzas.enums.TipoCuenta;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.mapper.ProductoMapper;
import prueba.finanzas.repository.ClienteRepository;
import prueba.finanzas.repository.ProductoRepository;
import prueba.finanzas.service.producto.GeneradorNumeroCuenta;
import prueba.finanzas.service.producto.ProductoServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private GeneradorNumeroCuenta generadorNumeroCuenta;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Cliente cliente;
    private Producto producto;
    private ProductoResponse productoResponse;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder().id(1L).nombres("Ana").apellidos("Gómez").build();

        producto = Producto.builder()
            .id(10L)
            .tipoCuenta(TipoCuenta.AHORROS)
            .numeroCuenta("5300000001")
            .estado(EstadoCuenta.ACTIVA)
            .saldo(BigDecimal.ZERO)
            .exentaGmf(false)
            .cliente(cliente)
            .build();

        productoResponse = ProductoResponse.builder()
            .id(10L)
            .tipoCuenta(TipoCuenta.AHORROS)
            .numeroCuenta("5300000001")
            .estado(EstadoCuenta.ACTIVA)
            .saldo(BigDecimal.ZERO)
            .exentaGmf(false)
            .clienteId(1L)
            .build();
    }

    @Test
    void crear_conClienteExistente_debeGenerarNumeroYGuardarCuentaActiva() {
        ProductoCreateRequest request = new ProductoCreateRequest();
        request.setTipoCuenta(TipoCuenta.AHORROS);
        request.setClienteId(1L);
        request.setSaldoInicial(BigDecimal.valueOf(100));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(generadorNumeroCuenta.generar(TipoCuenta.AHORROS)).thenReturn("5300000001");
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse);

        ProductoResponse resultado = productoService.crear(request);

        assertThat(resultado).isEqualTo(productoResponse);

        verify(productoRepository).save(argThat(p ->
            p.getEstado() == EstadoCuenta.ACTIVA
                && p.getNumeroCuenta().equals("5300000001")
                && p.getCliente().equals(cliente)
        ));
    }

    @Test
    void crear_conClienteInexistente_debeLanzarResourceNotFoundException() {
        ProductoCreateRequest request = new ProductoCreateRequest();
        request.setTipoCuenta(TipoCuenta.AHORROS);
        request.setClienteId(99L);

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.crear(request))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    void crear_cuentaAhorrosConSaldoInicialNegativo_debeLanzarBusinessRuleException() {
        ProductoCreateRequest request = new ProductoCreateRequest();
        request.setTipoCuenta(TipoCuenta.AHORROS);
        request.setClienteId(1L);
        request.setSaldoInicial(BigDecimal.valueOf(-50));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> productoService.crear(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("no puede tener saldo menor a $0");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void crear_sinSaldoInicial_debeUsarCero() {
        ProductoCreateRequest request = new ProductoCreateRequest();
        request.setTipoCuenta(TipoCuenta.CORRIENTE);
        request.setClienteId(1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(generadorNumeroCuenta.generar(TipoCuenta.CORRIENTE)).thenReturn("3300000001");
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse);

        productoService.crear(request);

        verify(productoRepository).save(argThat(p -> p.getSaldo().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void obtenerPorId_conIdExistente_debeRetornarProducto() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse);

        ProductoResponse resultado = productoService.obtenerPorId(10L);

        assertThat(resultado).isEqualTo(productoResponse);
    }

    @Test
    void obtenerPorId_conIdInexistente_debeLanzarResourceNotFoundException() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerTodos_debeRetornarListaMapeada() {
        when(productoRepository.findAll()).thenReturn(List.of(producto));
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse);

        List<ProductoResponse> resultado = productoService.obtenerTodos();

        assertThat(resultado).containsExactly(productoResponse);
    }

    @Test
    void obtenerPorCliente_conClienteExistente_debeRetornarProductos() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.findByClienteId(1L)).thenReturn(List.of(producto));
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse);

        List<ProductoResponse> resultado = productoService.obtenerPorCliente(1L);

        assertThat(resultado).containsExactly(productoResponse);
    }

    @Test
    void obtenerPorCliente_conClienteInexistente_debeLanzarResourceNotFoundException() {
        when(clienteRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productoService.obtenerPorCliente(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizar_cuentaActiva_debeActualizarEstadoYExencion() {
        ProductoUpdateRequest request = new ProductoUpdateRequest();
        request.setEstado(EstadoCuenta.INACTIVA);
        request.setExentaGmf(true);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse);

        productoService.actualizar(10L, request);

        assertThat(producto.getEstado()).isEqualTo(EstadoCuenta.INACTIVA);
        assertThat(producto.isExentaGmf()).isTrue();
        verify(productoRepository).save(producto);
    }

    @Test
    void actualizar_cuentaYaCancelada_debeLanzarBusinessRuleException() {
        producto.setEstado(EstadoCuenta.CANCELADA);
        ProductoUpdateRequest request = new ProductoUpdateRequest();
        request.setEstado(EstadoCuenta.ACTIVA);
        request.setExentaGmf(false);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> productoService.actualizar(10L, request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("cancelada");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizar_cancelarCuentaConSaldoDistintoDeCero_debeLanzarBusinessRuleException() {
        producto.setSaldo(BigDecimal.valueOf(500));
        ProductoUpdateRequest request = new ProductoUpdateRequest();
        request.setEstado(EstadoCuenta.CANCELADA);
        request.setExentaGmf(false);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> productoService.actualizar(10L, request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("saldo en $0");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizar_cancelarCuentaConSaldoCero_debeCancelarExitosamente() {
        producto.setSaldo(BigDecimal.ZERO);
        ProductoUpdateRequest request = new ProductoUpdateRequest();
        request.setEstado(EstadoCuenta.CANCELADA);
        request.setExentaGmf(false);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse);

        productoService.actualizar(10L, request);

        assertThat(producto.getEstado()).isEqualTo(EstadoCuenta.CANCELADA);
    }
}
