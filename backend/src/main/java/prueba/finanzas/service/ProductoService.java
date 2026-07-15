package prueba.finanzas.service;

import prueba.finanzas.dto.producto.ProductoCreateRequest;
import prueba.finanzas.dto.producto.ProductoResponse;
import prueba.finanzas.dto.producto.ProductoUpdateRequest;

import java.util.List;

public interface ProductoService {

    ProductoResponse crear(ProductoCreateRequest request);

    ProductoResponse obtenerPorId(Long id);

    List<ProductoResponse> obtenerTodos();

    List<ProductoResponse> obtenerPorCliente(Long clienteId);

    ProductoResponse actualizar(Long id, ProductoUpdateRequest request);

    void eliminar(Long id);
}
