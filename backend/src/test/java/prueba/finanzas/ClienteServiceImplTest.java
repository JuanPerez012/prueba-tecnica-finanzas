package prueba.finanzas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prueba.finanzas.dto.cliente.ClienteCreateRequest;
import prueba.finanzas.dto.cliente.ClienteResponse;
import prueba.finanzas.dto.cliente.ClienteUpdateRequest;
import prueba.finanzas.entity.Cliente;
import prueba.finanzas.enums.TipoIdentificacion;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.mapper.ClienteMapper;
import prueba.finanzas.port.ProductoConsultaPort;
import prueba.finanzas.repository.ClienteRepository;
import prueba.finanzas.service.cliente.ClienteServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoConsultaPort productoConsultaPort;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteCreateRequest createRequest;
    private Cliente clienteEntity;
    private ClienteResponse clienteResponse;

    @BeforeEach
    void setUp() {
        LocalDate fechaNacimientoMayorEdad = LocalDate.now().minusYears(25);

        createRequest = new ClienteCreateRequest();
        createRequest.setTipoIdentificacion(TipoIdentificacion.CC);
        createRequest.setNumeroIdentificacion("123456789");
        createRequest.setNombres("Ana");
        createRequest.setApellidos("Gómez");
        createRequest.setCorreoElectronico("ana.gomez@correo.com");
        createRequest.setFechaNacimiento(fechaNacimientoMayorEdad);

        clienteEntity = Cliente.builder()
            .id(1L)
            .tipoIdentificacion(TipoIdentificacion.CC)
            .numeroIdentificacion("123456789")
            .nombres("Ana")
            .apellidos("Gómez")
            .correoElectronico("ana.gomez@correo.com")
            .fechaNacimiento(fechaNacimientoMayorEdad)
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();

        clienteResponse = ClienteResponse.builder()
            .id(1L)
            .tipoIdentificacion(TipoIdentificacion.CC)
            .numeroIdentificacion("123456789")
            .nombres("Ana")
            .apellidos("Gómez")
            .correoElectronico("ana.gomez@correo.com")
            .fechaNacimiento(fechaNacimientoMayorEdad)
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();
    }

    @Test
    void crear_conDatosValidos_debeGuardarYRetornarCliente() {
        when(clienteRepository.existsByNumeroIdentificacion(createRequest.getNumeroIdentificacion())).thenReturn(false);
        when(clienteRepository.existsByCorreoElectronico(createRequest.getCorreoElectronico())).thenReturn(false);
        when(clienteMapper.toEntity(createRequest)).thenReturn(clienteEntity);
        when(clienteRepository.save(clienteEntity)).thenReturn(clienteEntity);
        when(clienteMapper.toResponse(clienteEntity)).thenReturn(clienteResponse);

        ClienteResponse resultado = clienteService.crear(createRequest);

        assertThat(resultado).isEqualTo(clienteResponse);
        verify(clienteRepository).save(clienteEntity);
    }

    @Test
    void crear_conClienteMenorDeEdad_debeLanzarBusinessRuleException() {
        createRequest.setFechaNacimiento(LocalDate.now().minusYears(10));

        assertThatThrownBy(() -> clienteService.crear(createRequest))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("mayor de edad");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crear_conNumeroIdentificacionDuplicado_debeLanzarBusinessRuleException() {
        when(clienteRepository.existsByNumeroIdentificacion(createRequest.getNumeroIdentificacion())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crear(createRequest))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("número de identificación");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crear_conCorreoDuplicado_debeLanzarBusinessRuleException() {
        when(clienteRepository.existsByNumeroIdentificacion(createRequest.getNumeroIdentificacion())).thenReturn(false);
        when(clienteRepository.existsByCorreoElectronico(createRequest.getCorreoElectronico())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crear(createRequest))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("correo electrónico");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_conIdExistente_debeRetornarCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEntity));
        when(clienteMapper.toResponse(clienteEntity)).thenReturn(clienteResponse);

        ClienteResponse resultado = clienteService.obtenerPorId(1L);

        assertThat(resultado).isEqualTo(clienteResponse);
    }

    @Test
    void obtenerPorId_conIdInexistente_debeLanzarResourceNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void obtenerTodos_debeRetornarListaMapeada() {
        when(clienteRepository.findAll()).thenReturn(List.of(clienteEntity));
        when(clienteMapper.toResponse(clienteEntity)).thenReturn(clienteResponse);

        List<ClienteResponse> resultado = clienteService.obtenerTodos();

        assertThat(resultado).hasSize(1).containsExactly(clienteResponse);
    }

    @Test
    void actualizar_conDatosValidos_debeActualizarYRetornarCliente() {
        ClienteUpdateRequest updateRequest = new ClienteUpdateRequest();
        updateRequest.setTipoIdentificacion(TipoIdentificacion.CC);
        updateRequest.setNumeroIdentificacion("123456789");
        updateRequest.setNombres("Ana María");
        updateRequest.setApellidos("Gómez");
        updateRequest.setCorreoElectronico("ana.gomez@correo.com");
        updateRequest.setFechaNacimiento(LocalDate.now().minusYears(25));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEntity));
        when(clienteRepository.existsByNumeroIdentificacionAndIdNot(updateRequest.getNumeroIdentificacion(), 1L)).thenReturn(false);
        when(clienteRepository.existsByCorreoElectronicoAndIdNot(updateRequest.getCorreoElectronico(), 1L)).thenReturn(false);
        when(clienteRepository.save(clienteEntity)).thenReturn(clienteEntity);
        when(clienteMapper.toResponse(clienteEntity)).thenReturn(clienteResponse);

        ClienteResponse resultado = clienteService.actualizar(1L, updateRequest);

        assertThat(resultado).isEqualTo(clienteResponse);
        verify(clienteMapper).actualizarEntity(clienteEntity, updateRequest);
        verify(clienteRepository).save(clienteEntity);
    }

    @Test
    void actualizar_conClienteInexistente_debeLanzarResourceNotFoundException() {
        ClienteUpdateRequest updateRequest = new ClienteUpdateRequest();
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizar(99L, updateRequest))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void actualizar_conNumeroIdentificacionDuplicadoDeOtroCliente_debeLanzarBusinessRuleException() {
        ClienteUpdateRequest updateRequest = new ClienteUpdateRequest();
        updateRequest.setNumeroIdentificacion("987654321");
        updateRequest.setFechaNacimiento(LocalDate.now().minusYears(25));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEntity));
        when(clienteRepository.existsByNumeroIdentificacionAndIdNot("987654321", 1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.actualizar(1L, updateRequest))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("otro cliente");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void eliminar_conClienteSinProductosAsociados_debeEliminarCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEntity));
        when(productoConsultaPort.tieneProductosAsociados(1L)).thenReturn(false);

        clienteService.eliminar(1L);

        verify(clienteRepository).delete(clienteEntity);
    }

    @Test
    void eliminar_conClienteConProductosAsociados_debeLanzarBusinessRuleException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEntity));
        when(productoConsultaPort.tieneProductosAsociados(1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.eliminar(1L))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("productos financieros asociados");

        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void eliminar_conClienteInexistente_debeLanzarResourceNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.eliminar(99L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(clienteRepository, never()).delete(any());
    }
}
