package prueba.finanzas.dto.producto;

import prueba.finanzas.enums.TipoCuenta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductoCreateRequest {

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private TipoCuenta tipoCuenta;

    @NotNull(message = "El cliente propietario de la cuenta es obligatorio")
    private Long clienteId;

    @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;

    private Boolean exentaGmf;
}
