package prueba.finanzas.service.cliente;

import prueba.finanzas.dto.cliente.ClienteCreateRequest;
import prueba.finanzas.dto.cliente.ClienteResponse;
import prueba.finanzas.dto.cliente.ClienteUpdateRequest;

import java.util.List;

public interface ClienteService {

    ClienteResponse crear(ClienteCreateRequest request);

    ClienteResponse obtenerPorId(Long id);

    List<ClienteResponse> obtenerTodos();

    ClienteResponse actualizar(Long id, ClienteUpdateRequest request);

    void eliminar(Long id);
}
