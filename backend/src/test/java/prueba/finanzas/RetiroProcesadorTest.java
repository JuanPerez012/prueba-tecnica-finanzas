package prueba.finanzas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.entity.Producto;
import prueba.finanzas.entity.Transaccion;
import prueba.finanzas.enums.EstadoCuenta;
import prueba.finanzas.enums.TipoMovimiento;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.repository.ProductoRepository;
import prueba.finanzas.service.transaccion.RetiroProcesador;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetiroProcesadorTest {

    @Mock
    private ProductoRepository productoRepository;

    private RetiroProcesador procesador;

    @BeforeEach
    void setUp() {
        procesador = new RetiroProcesador(productoRepository);
    }

    @Test
    void procesar_conSaldoSuficiente_debeDebitarYGenerarMovimientoDebito() {
        Producto origen = Producto.builder()
            .id(10L).numeroCuenta("5300000001").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.valueOf(200))
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(10L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(10L)).thenReturn(Optional.of(origen));

        Transaccion resultado = procesador.procesar(request);

        assertThat(origen.getSaldo()).isEqualByComparingTo("150");
        assertThat(resultado.getMovimientos()).hasSize(1);
        assertThat(resultado.getMovimientos().get(0).getTipoMovimiento()).isEqualTo(TipoMovimiento.DEBITO);
        verify(productoRepository).save(origen);
    }

    @Test
    void procesar_conSaldoInsuficiente_debeLanzarBusinessRuleExceptionYNoModificarSaldo() {
        Producto origen = Producto.builder()
            .id(10L).numeroCuenta("5300000001").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.valueOf(30))
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(10L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(10L)).thenReturn(Optional.of(origen));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Saldo insuficiente");

        assertThat(origen.getSaldo()).isEqualByComparingTo("30");
        verify(productoRepository, never()).save(any());
    }

    @Test
    void procesar_sinCuentaOrigen_debeLanzarBusinessRuleException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setMonto(BigDecimal.valueOf(50));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("cuenta origen");
    }

    @Test
    void procesar_conCuentaDestinoIndicada_debeLanzarBusinessRuleException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(10L);
        request.setProductoDestinoId(20L);
        request.setMonto(BigDecimal.valueOf(50));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("no debe indicar cuenta destino");
    }

    @Test
    void procesar_conCuentaOrigenInexistente_debeLanzarResourceNotFoundException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(999L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void procesar_conCuentaOrigenInactiva_debeLanzarBusinessRuleException() {
        Producto origen = Producto.builder()
            .id(10L).numeroCuenta("5300000001").estado(EstadoCuenta.CANCELADA).saldo(BigDecimal.ZERO)
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoOrigenId(10L);
        request.setMonto(BigDecimal.valueOf(10));

        when(productoRepository.bloquearPorId(10L)).thenReturn(Optional.of(origen));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("no está activa");
    }
}
