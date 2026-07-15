package prueba.finanzas.dto.transaccion;

import prueba.finanzas.enums.TipoTransaccion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransaccionCreateRequest {

    @NotNull(message = "El tipo de transacción es obligatorio")
    private TipoTransaccion tipoTransaccion;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a $0")
    private BigDecimal monto;

    private Long productoOrigenId;

    private Long productoDestinoId;

    @Size(max = 255, message = "La descripción no debe superar los 255 caracteres")
    private String descripcion;
}
