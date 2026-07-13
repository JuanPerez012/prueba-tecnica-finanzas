package prueba.finanzas.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class MayorEdadValidator implements ConstraintValidator<MayorEdad, LocalDate> {

    private int edadMinima;

    @Override
    public void initialize(MayorEdad constraintAnnotation) {
        this.edadMinima = constraintAnnotation.edadMinima();
    }

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        // Si es null, dejamos que @NotNull se encargue de reportar el error.
        if (fechaNacimiento == null) {
            return true;
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            return false;
        }
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        return edad >= edadMinima;
    }
}
