package prueba.finanzas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.entity.Movimiento;
import prueba.finanzas.entity.Producto;
import prueba.finanzas.entity.Transaccion;
import prueba.finanzas.enums.EstadoCuenta;
import prueba.finanzas.enums.TipoMovimiento;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.repository.ProductoRepository;
import prueba.finanzas.service.transaccion.TransferenciaProcesador;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaProcesadorTest {

    @Mock
    private ProductoRepository productoRepository;

    private TransferenciaProcesador procesador;

    @BeforeEach
    void setUp() {
        procesador = new TransferenciaProcesador(productoRepository);
    }

    @Test
    void procesar_conCuentasActivasYSaldoSuficiente_debeGenerarDebitoYCredito() {
        Producto origen = Producto.builder()
            .id(1L).numeroCuenta("5300000001").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.valueOf(300))
            .build();
        Producto destino = Producto.builder()
            .id(2L).numeroCuenta("3300000002").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.valueOf(100))
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(1L);
        request.setProductoDestinoId(2L);
        request.setMonto(BigDecimal.valueOf(100));

        when(productoRepository.bloquearPorId(1L)).thenReturn(Optional.of(origen));
        when(productoRepository.bloquearPorId(2L)).thenReturn(Optional.of(destino));

        Transaccion resultado = procesador.procesar(request);

        assertThat(origen.getSaldo()).isEqualByComparingTo("200");
        assertThat(destino.getSaldo()).isEqualByComparingTo("200");
        assertThat(resultado.getMovimientos()).hasSize(2);

        Movimiento debito = resultado.getMovimientos().stream()
            .filter(m -> m.getTipoMovimiento() == TipoMovimiento.DEBITO).findFirst().orElseThrow();
        Movimiento credito = resultado.getMovimientos().stream()
            .filter(m -> m.getTipoMovimiento() == TipoMovimiento.CREDITO).findFirst().orElseThrow();

        assertThat(debito.getProducto()).isEqualTo(origen);
        assertThat(credito.getProducto()).isEqualTo(destino);
        verify(productoRepository).save(origen);
        verify(productoRepository).save(destino);
    }

    @Test
    void procesar_conIdOrigenMayorQueIdDestino_debeBloquearEnOrdenAscendenteParaEvitarDeadlocks() {
        // origen tiene el id mayor (5) y destino el menor (2): el procesador debe
        // bloquear primero el id menor (2) y luego el mayor (5), sin importar
        // cuál sea origen y cuál destino en la petición.
        Producto origen = Producto.builder()
            .id(5L).numeroCuenta("5300000005").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.valueOf(500))
            .build();
        Producto destino = Producto.builder()
            .id(2L).numeroCuenta("3300000002").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.ZERO)
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(5L);
        request.setProductoDestinoId(2L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(2L)).thenReturn(Optional.of(destino));
        when(productoRepository.bloquearPorId(5L)).thenReturn(Optional.of(origen));

        procesador.procesar(request);

        InOrder orden = inOrder(productoRepository);
        orden.verify(productoRepository).bloquearPorId(2L);
        orden.verify(productoRepository).bloquearPorId(5L);
    }

    @Test
    void procesar_conSaldoInsuficienteEnOrigen_debeLanzarBusinessRuleException() {
        Producto origen = Producto.builder()
            .id(1L).numeroCuenta("5300000001").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.valueOf(10))
            .build();
        Producto destino = Producto.builder()
            .id(2L).numeroCuenta("3300000002").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.ZERO)
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(1L);
        request.setProductoDestinoId(2L);
        request.setMonto(BigDecimal.valueOf(100));

        when(productoRepository.bloquearPorId(1L)).thenReturn(Optional.of(origen));
        when(productoRepository.bloquearPorId(2L)).thenReturn(Optional.of(destino));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Saldo insuficiente");

        assertThat(destino.getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    void procesar_conMismaCuentaOrigenYDestino_debeLanzarBusinessRuleException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(1L);
        request.setProductoDestinoId(1L);
        request.setMonto(BigDecimal.valueOf(50));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("no pueden ser la misma");

        verify(productoRepository, never()).bloquearPorId(any());
    }

    @Test
    void procesar_sinCuentaOrigenOSinCuentaDestino_debeLanzarBusinessRuleException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(1L);
        request.setMonto(BigDecimal.valueOf(50));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("requiere cuenta origen");
    }

    @Test
    void procesar_conCuentaDestinoInactiva_debeLanzarBusinessRuleException() {
        Producto origen = Producto.builder()
            .id(1L).numeroCuenta("5300000001").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.valueOf(300))
            .build();
        Producto destino = Producto.builder()
            .id(2L).numeroCuenta("3300000002").estado(EstadoCuenta.INACTIVA).saldo(BigDecimal.ZERO)
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(1L);
        request.setProductoDestinoId(2L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(1L)).thenReturn(Optional.of(origen));
        when(productoRepository.bloquearPorId(2L)).thenReturn(Optional.of(destino));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("no está activa");
    }

    @Test
    void procesar_conCuentaOrigenInexistente_debeLanzarResourceNotFoundException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(1L);
        request.setProductoDestinoId(2L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
