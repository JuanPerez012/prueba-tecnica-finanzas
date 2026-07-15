package prueba.finanzas.dto.transaccion;

import prueba.finanzas.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MovimientoResponse {

    private Long id;
    private Long productoId;
    private String numeroCuenta;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private LocalDateTime fechaCreacion;
}
