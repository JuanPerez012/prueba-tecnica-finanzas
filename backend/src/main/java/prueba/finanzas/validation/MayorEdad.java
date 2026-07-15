package prueba.finanzas.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MayorEdadValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MayorEdad {

    String message() default "El cliente debe ser mayor de edad (18 años o más)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int edadMinima() default 18;
}
