package prueba.finanzas.service;

import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.dto.transaccion.TransaccionResponse;

import java.util.List;

public interface TransaccionService {

    TransaccionResponse crear(TransaccionCreateRequest request);

    TransaccionResponse obtenerPorId(Long id);

    List<TransaccionResponse> obtenerTodas();

    List<TransaccionResponse> obtenerPorProducto(Long productoId);
}
