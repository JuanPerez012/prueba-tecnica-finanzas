package prueba.finanzas.dto.transaccion;

import prueba.finanzas.enums.TipoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class TransaccionResponse {

    private Long id;
    private TipoTransaccion tipoTransaccion;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fechaCreacion;

    private Long productoOrigenId;
    private String productoOrigenNumeroCuenta;

    private Long productoDestinoId;
    private String productoDestinoNumeroCuenta;

    private List<MovimientoResponse> movimientos;
}
