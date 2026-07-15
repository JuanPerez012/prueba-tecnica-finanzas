package prueba.finanzas.mapper;

import prueba.finanzas.dto.transaccion.MovimientoResponse;
import prueba.finanzas.dto.transaccion.TransaccionResponse;
import prueba.finanzas.entity.Movimiento;
import prueba.finanzas.entity.Producto;
import prueba.finanzas.entity.Transaccion;
import org.springframework.stereotype.Component;

@Component
public class TransaccionMapper {

    public TransaccionResponse toResponse(Transaccion transaccion) {
        Producto origen = transaccion.getProductoOrigen();
        Producto destino = transaccion.getProductoDestino();

        return TransaccionResponse.builder()
            .id(transaccion.getId())
            .tipoTransaccion(transaccion.getTipoTransaccion())
            .monto(transaccion.getMonto())
            .descripcion(transaccion.getDescripcion())
            .fechaCreacion(transaccion.getFechaCreacion())
            .productoOrigenId(origen != null ? origen.getId() : null)
            .productoOrigenNumeroCuenta(origen != null ? origen.getNumeroCuenta() : null)
            .productoDestinoId(destino != null ? destino.getId() : null)
            .productoDestinoNumeroCuenta(destino != null ? destino.getNumeroCuenta() : null)
            .movimientos(transaccion.getMovimientos().stream().map(this::toMovimientoResponse).toList())
            .build();
    }

    public MovimientoResponse toMovimientoResponse(Movimiento movimiento) {
        return MovimientoResponse.builder()
            .id(movimiento.getId())
            .productoId(movimiento.getProducto().getId())
            .numeroCuenta(movimiento.getProducto().getNumeroCuenta())
            .tipoMovimiento(movimiento.getTipoMovimiento())
            .monto(movimiento.getMonto())
            .saldoResultante(movimiento.getSaldoResultante())
            .fechaCreacion(movimiento.getFechaCreacion())
            .build();
    }
}
