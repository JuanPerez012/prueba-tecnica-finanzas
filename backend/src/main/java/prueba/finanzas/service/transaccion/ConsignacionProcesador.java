package prueba.finanzas.service.transaccion;

import org.springframework.stereotype.Component;
import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.entity.Producto;
import prueba.finanzas.entity.Transaccion;
import prueba.finanzas.enums.TipoMovimiento;
import prueba.finanzas.enums.TipoTransaccion;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.repository.ProductoRepository;

import java.math.BigDecimal;

@Component
public class ConsignacionProcesador extends AbstractProcesadorTransaccion {

    public ConsignacionProcesador(ProductoRepository productoRepository) {
        super(productoRepository);
    }

    @Override
    public TipoTransaccion getTipo() {
        return TipoTransaccion.CONSIGNACION;
    }

    @Override
    public Transaccion procesar(TransaccionCreateRequest request) {
        if (request.getProductoDestinoId() == null) {
            throw new BusinessRuleException("La consignación requiere una cuenta destino (productoDestinoId)");
        }
        if (request.getProductoOrigenId() != null) {
            throw new BusinessRuleException("La consignación no debe indicar cuenta origen");
        }

        Producto destino = buscarProductoOLanzarExcepcion(request.getProductoDestinoId());
        validarCuentaActiva(destino);

        BigDecimal saldoResultante = acreditar(destino, request.getMonto());

        Transaccion transaccion = crearCabecera(request, null, destino);
        transaccion.agregarMovimiento(nuevoMovimiento(destino, TipoMovimiento.CREDITO, request.getMonto(), saldoResultante));
        return transaccion;
    }
}
