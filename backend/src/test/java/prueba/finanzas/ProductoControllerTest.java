package prueba.finanzas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import prueba.finanzas.controller.ProductoController;
import prueba.finanzas.dto.producto.ProductoCreateRequest;
import prueba.finanzas.dto.producto.ProductoResponse;
import prueba.finanzas.dto.producto.ProductoUpdateRequest;
import prueba.finanzas.enums.EstadoCuenta;
import prueba.finanzas.enums.TipoCuenta;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.GlobalExceptionHandler;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.service.producto.ProductoService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@Import(GlobalExceptionHandler.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductoService productoService;

    private ProductoResponse respuestaEjemplo() {
        return ProductoResponse.builder()
            .id(10L)
            .tipoCuenta(TipoCuenta.AHORROS)
            .numeroCuenta("5300000001")
            .estado(EstadoCuenta.ACTIVA)
            .saldo(BigDecimal.valueOf(100))
            .exentaGmf(false)
            .clienteId(1L)
            .clienteNombreCompleto("Ana Gómez")
            .clienteNumeroIdentificacion("123456789")
            .build();
    }

    @Test
    void crear_conDatosValidos_debeRetornar201() throws Exception {
        ProductoCreateRequest request = new ProductoCreateRequest();
        request.setTipoCuenta(TipoCuenta.AHORROS);
        request.setClienteId(1L);
        request.setSaldoInicial(BigDecimal.valueOf(100));

        when(productoService.crear(any(ProductoCreateRequest.class))).thenReturn(respuestaEjemplo());

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/productos/10"))
            .andExpect(jsonPath("$.numeroCuenta").value("5300000001"));
    }

    @Test
    void crear_conClienteIdFaltante_debeRetornar400() throws Exception {
        ProductoCreateRequest request = new ProductoCreateRequest();
        request.setTipoCuenta(TipoCuenta.AHORROS);

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conClienteInexistente_debeRetornar404() throws Exception {
        ProductoCreateRequest request = new ProductoCreateRequest();
        request.setTipoCuenta(TipoCuenta.AHORROS);
        request.setClienteId(99L);

        when(productoService.crear(any(ProductoCreateRequest.class)))
            .thenThrow(new ResourceNotFoundException("No existe un cliente con id: 99"));

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void crear_conSaldoInicialNegativo_debeRetornar400PorValidacionDelDto() throws Exception {
        ProductoCreateRequest request = new ProductoCreateRequest();
        request.setTipoCuenta(TipoCuenta.AHORROS);
        request.setClienteId(1L);
        request.setSaldoInicial(BigDecimal.valueOf(-10));

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Error"))
            .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.containsString("saldoInicial")));

        verify(productoService, never()).crear(any());
    }

    @Test
    void obtenerTodos_debeRetornar200ConListaDeProductos() throws Exception {
        when(productoService.obtenerTodos()).thenReturn(List.of(respuestaEjemplo()));

        mockMvc.perform(get("/api/v1/productos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerPorId_conIdExistente_debeRetornar200() throws Exception {
        when(productoService.obtenerPorId(10L)).thenReturn(respuestaEjemplo());

        mockMvc.perform(get("/api/v1/productos/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void obtenerPorCliente_debeRetornar200ConProductosDelCliente() throws Exception {
        when(productoService.obtenerPorCliente(1L)).thenReturn(List.of(respuestaEjemplo()));

        mockMvc.perform(get("/api/v1/productos/cliente/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].clienteId").value(1));
    }

    @Test
    void actualizar_cancelandoCuentaConSaldoDistintoDeCero_debeRetornar422() throws Exception {
        ProductoUpdateRequest request = new ProductoUpdateRequest();
        request.setEstado(EstadoCuenta.CANCELADA);
        request.setExentaGmf(false);

        when(productoService.actualizar(eq(10L), any(ProductoUpdateRequest.class)))
            .thenThrow(new BusinessRuleException("Solo se pueden cancelar cuentas con saldo en $0"));

        mockMvc.perform(put("/api/v1/productos/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void actualizar_conDatosValidos_debeRetornar200() throws Exception {
        ProductoUpdateRequest request = new ProductoUpdateRequest();
        request.setEstado(EstadoCuenta.INACTIVA);
        request.setExentaGmf(true);

        when(productoService.actualizar(eq(10L), any(ProductoUpdateRequest.class))).thenReturn(respuestaEjemplo());

        mockMvc.perform(put("/api/v1/productos/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
}
