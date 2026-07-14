package prueba.finanzas.dto.producto;

import prueba.finanzas.enums.EstadoCuenta;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoUpdateRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoCuenta estado;

    @NotNull(message = "Debes indicar si la cuenta está exenta de GMF")
    private Boolean exentaGmf;
}
