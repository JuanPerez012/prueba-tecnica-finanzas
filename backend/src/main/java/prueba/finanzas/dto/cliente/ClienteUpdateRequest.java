package prueba.finanzas.dto.cliente;

import prueba.finanzas.enums.TipoIdentificacion;
import prueba.finanzas.validation.MayorEdad;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClienteUpdateRequest {

    @NotNull(message = "El tipo de identificación es obligatorio")
    private TipoIdentificacion tipoIdentificacion;

    @NotBlank(message = "El número de identificación es obligatorio")
    @Size(max = 30, message = "El número de identificación no debe superar los 30 caracteres")
    private String numeroIdentificacion;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 100, message = "Los nombres deben tener al menos 2 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener al menos 2 caracteres")
    private String apellidos;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido (xxxx@xxxxx.xxx)")
    @Pattern(
        regexp = "^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$",
        message = "El correo electrónico no tiene un formato válido (xxxx@xxxxx.xxx)"
    )
    private String correoElectronico;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    @MayorEdad
    private LocalDate fechaNacimiento;
}
