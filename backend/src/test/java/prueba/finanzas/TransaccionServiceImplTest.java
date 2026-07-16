package prueba.finanzas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.dto.transaccion.TransaccionResponse;
import prueba.finanzas.entity.Transaccion;
import prueba.finanzas.enums.TipoTransaccion;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.mapper.TransaccionMapper;
import prueba.finanzas.repository.ProductoRepository;
import prueba.finanzas.repository.TransaccionRepository;
import prueba.finanzas.service.transaccion.ProcesadorTransaccion;
import prueba.finanzas.service.transaccion.TransaccionServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceImplTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private TransaccionMapper transaccionMapper;

    @Mock
    private ProcesadorTransaccion consignacionProcesador;

    @Mock
    private ProcesadorTransaccion retiroProcesador;

    private TransaccionServiceImpl transaccionService;

    @BeforeEach
    void setUp() {
        lenient().when(consignacionProcesador.getTipo()).thenReturn(TipoTransaccion.CONSIGNACION);
        lenient().when(retiroProcesador.getTipo()).thenReturn(TipoTransaccion.RETIRO);

        transaccionService = new TransaccionServiceImpl(
            transaccionRepository,
            productoRepository,
            transaccionMapper,
            List.of(consignacionProcesador, retiroProcesador)
        );
    }

    @Test
    void crear_conTipoSoportado_debeDelegarEnElProcesadorCorrecto() {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setTipoTransaccion(TipoTransaccion.CONSIGNACION);
        request.setMonto(BigDecimal.valueOf(1000));
        request.setProductoDestinoId(10L);

        Transaccion transaccionProcesada = Transaccion.builder().tipoTransaccion(TipoTransaccion.CONSIGNACION).build();
        Transaccion transaccionGuardada = Transaccion.builder().id(1L).tipoTransaccion(TipoTransaccion.CONSIGNACION).build();
        TransaccionResponse response = TransaccionResponse.builder().id(1L).tipoTransaccion(TipoTransaccion.CONSIGNACION).build();

        when(consignacionProcesador.procesar(request)).thenReturn(transaccionProcesada);
        when(transaccionRepository.save(transaccionProcesada)).thenReturn(transaccionGuardada);
        when(transaccionMapper.toResponse(transaccionGuardada)).thenReturn(response);

        TransaccionResponse resultado = transaccionService.crear(request);

        assertThat(resultado).isEqualTo(response);
        verify(consignacionProcesador).procesar(request);
        verify(retiroProcesador, never()).procesar(any());
        verify(transaccionRepository).save(transaccionProcesada);
    }

    @Test
    void crear_conTipoNoSoportado_debeLanzarBusinessRuleException() {
        // Servicio construido sin procesadores registrados para simular un tipo sin implementación
        TransaccionServiceImpl servicioSinProcesadores = new TransaccionServiceImpl(
            transaccionRepository, productoRepository, transaccionMapper, List.of()
        );

        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA);
        request.setMonto(BigDecimal.TEN);

        assertThatThrownBy(() -> servicioSinProcesadores.crear(request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("no soportado");

        verify(transaccionRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_conIdExistente_debeRetornarTransaccion() {
        Transaccion transaccion = Transaccion.builder().id(1L).build();
        TransaccionResponse response = TransaccionResponse.builder().id(1L).build();

        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        when(transaccionMapper.toResponse(transaccion)).thenReturn(response);

        TransaccionResponse resultado = transaccionService.obtenerPorId(1L);

        assertThat(resultado).isEqualTo(response);
    }

    @Test
    void obtenerPorId_conIdInexistente_debeLanzarResourceNotFoundException() {
        when(transaccionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transaccionService.obtenerPorId(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerTodas_debeRetornarListaMapeada() {
        Transaccion transaccion = Transaccion.builder().id(1L).build();
        TransaccionResponse response = TransaccionResponse.builder().id(1L).build();

        when(transaccionRepository.findAll()).thenReturn(List.of(transaccion));
        when(transaccionMapper.toResponse(transaccion)).thenReturn(response);

        List<TransaccionResponse> resultado = transaccionService.obtenerTodas();

        assertThat(resultado).containsExactly(response);
    }

    @Test
    void obtenerPorProducto_conProductoExistente_debeRetornarTransacciones() {
        Transaccion transaccion = Transaccion.builder().id(1L).build();
        TransaccionResponse response = TransaccionResponse.builder().id(1L).build();

        when(productoRepository.existsById(10L)).thenReturn(true);
        when(transaccionRepository.findByProductoOrigenIdOrProductoDestinoIdOrderByFechaCreacionDesc(10L, 10L))
            .thenReturn(List.of(transaccion));
        when(transaccionMapper.toResponse(transaccion)).thenReturn(response);

        List<TransaccionResponse> resultado = transaccionService.obtenerPorProducto(10L);

        assertThat(resultado).containsExactly(response);
    }

    @Test
    void obtenerPorProducto_conProductoInexistente_debeLanzarResourceNotFoundException() {
        when(productoRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> transaccionService.obtenerPorProducto(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
