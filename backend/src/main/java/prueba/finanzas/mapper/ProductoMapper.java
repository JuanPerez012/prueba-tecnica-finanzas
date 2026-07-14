package prueba.finanzas.mapper;

import prueba.finanzas.entity.Producto;
import prueba.finanzas.dto.producto.ProductoResponse;

import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoResponse toResponse(Producto producto) {
        return ProductoResponse.builder()
            .id(producto.getId())
            .tipoCuenta(producto.getTipoCuenta())
            .numeroCuenta(producto.getNumeroCuenta())
            .estado(producto.getEstado())
            .saldo(producto.getSaldo())
            .exentaGmf(producto.isExentaGmf())
            .fechaCreacion(producto.getFechaCreacion())
            .fechaModificacion(producto.getFechaModificacion())
            .clienteId(producto.getCliente().getId())
            .clienteNombreCompleto(producto.getCliente().getNombres() + " " + producto.getCliente().getApellidos())
            .clienteNumeroIdentificacion(producto.getCliente().getNumeroIdentificacion())
            .build();
    }
}
