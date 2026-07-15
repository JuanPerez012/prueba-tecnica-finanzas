package prueba.finanzas.service.transaccion;

import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.entity.Movimiento;
import prueba.finanzas.entity.Producto;
import prueba.finanzas.entity.Transaccion;
import prueba.finanzas.enums.EstadoCuenta;
import prueba.finanzas.enums.TipoMovimiento;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.repository.ProductoRepository;

import java.math.BigDecimal;

public abstract class AbstractProcesadorTransaccion implements ProcesadorTransaccion {

    protected final ProductoRepository productoRepository;

    protected AbstractProcesadorTransaccion(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    protected Producto buscarProductoOLanzarExcepcion(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    protected void validarCuentaActiva(Producto producto) {
        if (producto.getEstado() != EstadoCuenta.ACTIVA) {
            throw new BusinessRuleException(
                "La cuenta " + producto.getNumeroCuenta() + " no está activa y no puede operar transacciones"
            );
        }
    }

    protected BigDecimal debitar(Producto producto, BigDecimal monto) {
        BigDecimal saldoResultante = producto.getSaldo().subtract(monto);
        if (saldoResultante.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(
                "Saldo insuficiente en la cuenta " + producto.getNumeroCuenta() + " para realizar la operación"
            );
        }
        producto.setSaldo(saldoResultante);
        productoRepository.save(producto);
        return saldoResultante;
    }

    protected BigDecimal acreditar(Producto producto, BigDecimal monto) {
        BigDecimal saldoResultante = producto.getSaldo().add(monto);
        producto.setSaldo(saldoResultante);
        productoRepository.save(producto);
        return saldoResultante;
    }

    protected Transaccion crearCabecera(TransaccionCreateRequest request, Producto origen, Producto destino) {
        return Transaccion.builder()
            .tipoTransaccion(request.getTipoTransaccion())
            .monto(request.getMonto())
            .descripcion(request.getDescripcion())
            .productoOrigen(origen)
            .productoDestino(destino)
            .build();
    }

    protected Movimiento nuevoMovimiento(Producto producto, TipoMovimiento tipo, BigDecimal monto, BigDecimal saldoResultante) {
        return Movimiento.builder()
            .producto(producto)
            .tipoMovimiento(tipo)
            .monto(monto)
            .saldoResultante(saldoResultante)
            .build();
    }
}
