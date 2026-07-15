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
public class RetiroProcesador extends AbstractProcesadorTransaccion {

    public RetiroProcesador(ProductoRepository productoRepository) {
        super(productoRepository);
    }

    @Override
    public TipoTransaccion getTipo() {
        return TipoTransaccion.RETIRO;
    }

    @Override
    public Transaccion procesar(TransaccionCreateRequest request) {
        if (request.getProductoOrigenId() == null) {
            throw new BusinessRuleException("El retiro requiere una cuenta origen (productoOrigenId)");
        }
        if (request.getProductoDestinoId() != null) {
            throw new BusinessRuleException("El retiro no debe indicar cuenta destino");
        }

        Producto origen = bloquearProductoOLanzarExcepcion(request.getProductoOrigenId());
        validarCuentaActiva(origen);

        BigDecimal saldoResultante = debitar(origen, request.getMonto());

        Transaccion transaccion = crearCabecera(request, origen, null);
        transaccion.agregarMovimiento(nuevoMovimiento(origen, TipoMovimiento.DEBITO, request.getMonto(), saldoResultante));
        return transaccion;
    }
}
