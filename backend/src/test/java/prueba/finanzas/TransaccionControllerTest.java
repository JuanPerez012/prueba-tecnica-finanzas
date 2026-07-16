package prueba.finanzas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import prueba.finanzas.controller.TransaccionController;
import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.dto.transaccion.TransaccionResponse;
import prueba.finanzas.enums.TipoTransaccion;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.GlobalExceptionHandler;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.service.transaccion.TransaccionService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransaccionController.class)
@Import(GlobalExceptionHandler.class)
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransaccionService transaccionService;

    private TransaccionResponse respuestaEjemplo() {
        return TransaccionResponse.builder()
            .id(1L)
            .tipoTransaccion(TipoTransaccion.CONSIGNACION)
            .monto(BigDecimal.valueOf(100))
            .productoDestinoId(10L)
            .productoDestinoNumeroCuenta("5300000001")
            .movimientos(List.of())
            .build();
    }

    @Test
    void crear_consignacionValida_debeRetornar201() throws Exception {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setTipoTransaccion(TipoTransaccion.CONSIGNACION);
        request.setMonto(BigDecimal.valueOf(100));
        request.setProductoDestinoId(10L);

        when(transaccionService.crear(any(TransaccionCreateRequest.class))).thenReturn(respuestaEjemplo());

        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/transacciones/1"))
            .andExpect(jsonPath("$.tipoTransaccion").value("CONSIGNACION"));
    }

    @Test
    void crear_conMontoNegativo_debeRetornar400() throws Exception {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setTipoTransaccion(TipoTransaccion.RETIRO);
        request.setMonto(BigDecimal.valueOf(-10));
        request.setProductoOrigenId(10L);

        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crear_retiroConSaldoInsuficiente_debeRetornar422() throws Exception {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setTipoTransaccion(TipoTransaccion.RETIRO);
        request.setMonto(BigDecimal.valueOf(500));
        request.setProductoOrigenId(10L);

        when(transaccionService.crear(any(TransaccionCreateRequest.class)))
            .thenThrow(new BusinessRuleException("Saldo insuficiente en la cuenta 5300000001 para realizar la operación"));

        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void crear_conProductoInexistente_debeRetornar404() throws Exception {
        TransaccionCreateRequest request = new TransaccionCreateRequest();
        request.setTipoTransaccion(TipoTransaccion.RETIRO);
        request.setMonto(BigDecimal.valueOf(50));
        request.setProductoOrigenId(999L);

        when(transaccionService.crear(any(TransaccionCreateRequest.class)))
            .thenThrow(new ResourceNotFoundException("Producto no encontrado con id: 999"));

        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void obtenerTodas_debeRetornar200ConListaDeTransacciones() throws Exception {
        when(transaccionService.obtenerTodas()).thenReturn(List.of(respuestaEjemplo()));

        mockMvc.perform(get("/api/v1/transacciones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerPorId_conIdExistente_debeRetornar200() throws Exception {
        when(transaccionService.obtenerPorId(1L)).thenReturn(respuestaEjemplo());

        mockMvc.perform(get("/api/v1/transacciones/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPorId_conIdInexistente_debeRetornar404() throws Exception {
        when(transaccionService.obtenerPorId(99L))
            .thenThrow(new ResourceNotFoundException("Transacción no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/transacciones/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorProducto_debeRetornar200ConTransaccionesDelProducto() throws Exception {
        when(transaccionService.obtenerPorProducto(10L)).thenReturn(List.of(respuestaEjemplo()));

        mockMvc.perform(get("/api/v1/transacciones/producto/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].productoDestinoId").value(10));
    }
}
