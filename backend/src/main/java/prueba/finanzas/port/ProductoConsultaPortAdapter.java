package prueba.finanzas.port;

import prueba.finanzas.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoConsultaPortAdapter implements ProductoConsultaPort {

    private final ProductoRepository productoRepository;

    @Override
    public boolean tieneProductosAsociados(Long clienteId) {
        return productoRepository.existsByClienteId(clienteId);
    }
}
