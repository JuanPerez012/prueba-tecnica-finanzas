package prueba.finanzas.service.transaccion;

import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.entity.Transaccion;
import prueba.finanzas.enums.TipoTransaccion;

public interface ProcesadorTransaccion {

    TipoTransaccion getTipo();

    Transaccion procesar(TransaccionCreateRequest request);
}
