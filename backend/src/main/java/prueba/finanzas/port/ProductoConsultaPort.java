package prueba.finanzas.port;

/**
 * Puerto (en el sentido de arquitectura hexagonal) que desacopla el
 * módulo de Clientes del módulo de Productos, que aún no se ha
 * implementado. La regla de negocio "un cliente no puede eliminarse si
 * tiene productos financieros asociados" vive en ClienteService, pero
 * la consulta real la resuelve quien implemente esta interfaz.
 */
public interface ProductoConsultaPort {

    /**
     * @param clienteId id del cliente a verificar
     * @return true si el cliente tiene al menos un producto financiero asociado
     */
    boolean tieneProductosAsociados(Long clienteId);
}
