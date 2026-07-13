package prueba.finanzas.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio del dominio
 * (ej. cliente menor de edad, identificación duplicada, cliente con
 * productos asociados al intentar eliminarlo, etc.).
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
