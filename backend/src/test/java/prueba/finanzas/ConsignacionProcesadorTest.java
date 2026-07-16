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
import prueba.finanzas.service.transaccion.ConsignacionProcesador;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsignacionProcesadorTest {

    @Mock
    private ProductoRepository productoRepository;

    private ConsignacionProcesador procesador;

    @BeforeEach
    void setUp() {
        procesador = new ConsignacionProcesador(productoRepository);
    }

    @Test
    void getTipo_debeRetornarConsignacion() {
        assertThat(procesador.getTipo().name()).isEqualTo("CONSIGNACION");
    }

    @Test
    void procesar_conCuentaDestinoActiva_debeAcreditarSaldoYGenerarMovimientoCredito() {
        Producto destino = Producto.builder()
            .id(10L).numeroCuenta("5300000001").estado(EstadoCuenta.ACTIVA).saldo(BigDecimal.valueOf(100))
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoDestinoId(10L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(10L)).thenReturn(Optional.of(destino));

        Transaccion resultado = procesador.procesar(request);

        assertThat(destino.getSaldo()).isEqualByComparingTo("150");
        assertThat(resultado.getMovimientos()).hasSize(1);
        assertThat(resultado.getMovimientos().get(0).getTipoMovimiento()).isEqualTo(TipoMovimiento.CREDITO);
        assertThat(resultado.getMovimientos().get(0).getSaldoResultante()).isEqualByComparingTo("150");
        verify(productoRepository).save(destino);
    }

    @Test
    void procesar_sinCuentaDestino_debeLanzarBusinessRuleException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setMonto(BigDecimal.valueOf(50));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("cuenta destino");
    }

    @Test
    void procesar_conCuentaOrigenIndicada_debeLanzarBusinessRuleException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoDestinoId(10L);
        request.setProductoOrigenId(5L);
        request.setMonto(BigDecimal.valueOf(50));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("no debe indicar cuenta origen");
    }

    @Test
    void procesar_conCuentaDestinoInexistente_debeLanzarResourceNotFoundException() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoDestinoId(999L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void procesar_conCuentaDestinoInactiva_debeLanzarBusinessRuleException() {
        Producto destino = Producto.builder()
            .id(10L).numeroCuenta("5300000001").estado(EstadoCuenta.INACTIVA).saldo(BigDecimal.ZERO)
            .build();

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setProductoDestinoId(10L);
        request.setMonto(BigDecimal.valueOf(50));

        when(productoRepository.bloquearPorId(10L)).thenReturn(Optional.of(destino));

        assertThatThrownBy(() -> procesador.procesar(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("no está activa");

        verify(productoRepository, never()).save(any());
    }
}
