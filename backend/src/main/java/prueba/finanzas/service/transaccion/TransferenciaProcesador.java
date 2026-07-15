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
public class TransferenciaProcesador extends AbstractProcesadorTransaccion {

    public TransferenciaProcesador(ProductoRepository productoRepository) {
        super(productoRepository);
    }

    @Override
    public TipoTransaccion getTipo() {
        return TipoTransaccion.TRANSFERENCIA;
    }

    @Override
    public Transaccion procesar(TransaccionCreateRequest request) {
        Long idOrigen = request.getProductoOrigenId();
        Long idDestino = request.getProductoDestinoId();

        if (idOrigen == null || idDestino == null) {
            throw new BusinessRuleException(
                "La transferencia requiere cuenta origen (productoOrigenId) y cuenta destino (productoDestinoId)"
            );
        }
        if (idOrigen.equals(idDestino)) {
            throw new BusinessRuleException("La cuenta origen y la cuenta destino no pueden ser la misma");
        }

        Long idMenor = Math.min(idOrigen, idDestino);
        Long idMayor = Math.max(idOrigen, idDestino);

        Producto cuentaIdMenor = bloquearProductoOLanzarExcepcion(idMenor);
        Producto cuentaIdMayor = bloquearProductoOLanzarExcepcion(idMayor);

        Producto origen = idOrigen.equals(idMenor) ? cuentaIdMenor : cuentaIdMayor;
        Producto destino = idDestino.equals(idMenor) ? cuentaIdMenor : cuentaIdMayor;

        validarCuentaActiva(origen);
        validarCuentaActiva(destino);

        BigDecimal saldoResultanteOrigen = debitar(origen, request.getMonto());
        BigDecimal saldoResultanteDestino = acreditar(destino, request.getMonto());

        Transaccion transaccion = crearCabecera(request, origen, destino);
        transaccion.agregarMovimiento(nuevoMovimiento(origen, TipoMovimiento.DEBITO, request.getMonto(), saldoResultanteOrigen));
        transaccion.agregarMovimiento(nuevoMovimiento(destino, TipoMovimiento.CREDITO, request.getMonto(), saldoResultanteDestino));
        return transaccion;
    }
}
