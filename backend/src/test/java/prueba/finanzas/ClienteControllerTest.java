package prueba.finanzas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import prueba.finanzas.controller.ClienteController;
import prueba.finanzas.dto.cliente.ClienteCreateRequest;
import prueba.finanzas.dto.cliente.ClienteResponse;
import prueba.finanzas.dto.cliente.ClienteUpdateRequest;
import prueba.finanzas.enums.TipoIdentificacion;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.GlobalExceptionHandler;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.service.cliente.ClienteService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    private ClienteResponse respuestaEjemplo() {
        return ClienteResponse.builder()
            .id(1L)
            .tipoIdentificacion(TipoIdentificacion.CC)
            .numeroIdentificacion("123456789")
            .nombres("Ana")
            .apellidos("Gómez")
            .correoElectronico("ana.gomez@correo.com")
            .fechaNacimiento(LocalDate.now().minusYears(25))
            .build();
    }

    private ClienteCreateRequest requestValido() {
        ClienteCreateRequest request = new ClienteCreateRequest();
        request.setTipoIdentificacion(TipoIdentificacion.CC);
        request.setNumeroIdentificacion("123456789");
        request.setNombres("Ana");
        request.setApellidos("Gómez");
        request.setCorreoElectronico("ana.gomez@correo.com");
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        return request;
    }

    @Test
    void crear_conDatosValidos_debeRetornar201ConUbicacion() throws Exception {
        when(clienteService.crear(any(ClienteCreateRequest.class))).thenReturn(respuestaEjemplo());

        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido())))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/clientes/1"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombres").value("Ana"));
    }

    @Test
    void crear_conCorreoInvalido_debeRetornar400() throws Exception {
        ClienteCreateRequest request = requestValido();
        request.setCorreoElectronico("correo-no-valido");

        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(clienteService, never()).crear(any());
    }

    @Test
    void crear_conNombreVacio_debeRetornar400() throws Exception {
        ClienteCreateRequest request = requestValido();
        request.setNombres("");

        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(clienteService, never()).crear(any());
    }

    @Test
    void crear_conClienteMenorDeEdad_debeRetornar422() throws Exception {
        when(clienteService.crear(any(ClienteCreateRequest.class)))
            .thenThrow(new BusinessRuleException("El cliente debe ser mayor de edad (18 años o más)"));

        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido())))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("Business Rule Violation"));
    }

    @Test
    void obtenerTodos_debeRetornar200ConListaDeClientes() throws Exception {
        when(clienteService.obtenerTodos()).thenReturn(List.of(respuestaEjemplo()));

        mockMvc.perform(get("/api/v1/clientes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].numeroIdentificacion").value("123456789"));
    }

    @Test
    void obtenerPorId_conIdExistente_debeRetornar200() throws Exception {
        when(clienteService.obtenerPorId(1L)).thenReturn(respuestaEjemplo());

        mockMvc.perform(get("/api/v1/clientes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPorId_conIdInexistente_debeRetornar404() throws Exception {
        when(clienteService.obtenerPorId(99L))
            .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/clientes/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void actualizar_conDatosValidos_debeRetornar200() throws Exception {
        ClienteUpdateRequest request = new ClienteUpdateRequest();
        request.setTipoIdentificacion(TipoIdentificacion.CC);
        request.setNumeroIdentificacion("123456789");
        request.setNombres("Ana María");
        request.setApellidos("Gómez");
        request.setCorreoElectronico("ana.gomez@correo.com");
        request.setFechaNacimiento(LocalDate.now().minusYears(25));

        when(clienteService.actualizar(eq(1L), any(ClienteUpdateRequest.class))).thenReturn(respuestaEjemplo());

        mockMvc.perform(put("/api/v1/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminar_conClienteSinProductos_debeRetornar204() throws Exception {
        doNothing().when(clienteService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/clientes/1"))
            .andExpect(status().isNoContent());

        verify(clienteService).eliminar(1L);
    }

    @Test
    void eliminar_conClienteConProductosAsociados_debeRetornar422() throws Exception {
        doThrow(new BusinessRuleException("No se puede eliminar el cliente porque tiene productos financieros asociados"))
            .when(clienteService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/clientes/1"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("Business Rule Violation"));
    }
}
