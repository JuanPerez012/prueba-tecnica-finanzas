package prueba.finanzas.port;

import org.springframework.stereotype.Component;

/**
 * Implementación temporal mientras el módulo de Productos no existe.
 *
 * TODO: eliminar esta clase y reemplazarla por un adaptador real, por
 * ejemplo:
 *
 *   @Component
 *   @RequiredArgsConstructor
 *   public class ProductoConsultaPortImpl implements ProductoConsultaPort {
 *       private final ProductoRepository productoRepository;
 *       public boolean tieneProductosAsociados(Long clienteId) {
 *           return productoRepository.existsByClienteId(clienteId);
 *       }
 *   }
 */
@Component
public class ProductoConsultaPortStub implements ProductoConsultaPort {

    @Override
    public boolean tieneProductosAsociados(Long clienteId) {
        return false;
    }
}
