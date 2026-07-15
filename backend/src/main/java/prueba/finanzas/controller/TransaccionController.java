package prueba.finanzas.controller;

import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.dto.transaccion.TransaccionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prueba.finanzas.service.TransaccionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;

    @PostMapping
    public ResponseEntity<TransaccionResponse> crear(@Valid @RequestBody TransaccionCreateRequest request) {
        TransaccionResponse creada = transaccionService.crear(request);
        URI location = URI.create("/api/v1/transacciones/" + creada.getId());
        return ResponseEntity.created(location).body(creada);
    }

    @GetMapping
    public ResponseEntity<List<TransaccionResponse>> obtenerTodas() {
        return ResponseEntity.ok(transaccionService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerPorId(id));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<TransaccionResponse>> obtenerPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(transaccionService.obtenerPorProducto(productoId));
    }
}
