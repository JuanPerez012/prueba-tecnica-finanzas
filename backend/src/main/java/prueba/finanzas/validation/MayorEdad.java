package prueba.finanzas.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Regla de negocio: "Un cliente no podrá ser creado ni existir en la
 * base de datos si es menor de edad".
 * Se aplica sobre el campo fechaNacimiento en los DTOs de entrada.
 */
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
