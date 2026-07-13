package prueba.finanzas.enums;

/**
 * Tipos de identificación soportados para un Cliente.
 * Se representa como String (EnumType.STRING) en base de datos
 * para mantener la legibilidad de los datos persistidos.
 */
public enum TipoIdentificacion {
    CC,   // Cédula de ciudadanía
    CE,   // Cédula de extranjería
    TI,   // Tarjeta de identidad
    PA,   // Pasaporte
    NIT   // Número de identificación tributaria
}
