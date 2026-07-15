package prueba.finanzas.controller;

import prueba.finanzas.dto.producto.ProductoCreateRequest;
import prueba.finanzas.dto.producto.ProductoResponse;
import prueba.finanzas.dto.producto.ProductoUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prueba.finanzas.service.producto.ProductoService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoCreateRequest request) {
        ProductoResponse creado = productoService.crear(request);
        URI location = URI.create("/api/v1/productos/" + creado.getId());
        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ProductoResponse>> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(productoService.obtenerPorCliente(clienteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody ProductoUpdateRequest request
    ) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }
}
