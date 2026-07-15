package prueba.finanzas.service.transaccion;

import prueba.finanzas.dto.transaccion.TransaccionCreateRequest;
import prueba.finanzas.dto.transaccion.TransaccionResponse;
import prueba.finanzas.entity.Transaccion;
import prueba.finanzas.enums.TipoTransaccion;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import prueba.finanzas.mapper.TransaccionMapper;
import prueba.finanzas.repository.ProductoRepository;
import prueba.finanzas.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final ProductoRepository productoRepository;
    private final TransaccionMapper transaccionMapper;
    private final Map<TipoTransaccion, ProcesadorTransaccion> procesadoresPorTipo;

    public TransaccionServiceImpl(
        TransaccionRepository transaccionRepository,
        ProductoRepository productoRepository,
        TransaccionMapper transaccionMapper,
        List<ProcesadorTransaccion> procesadores
    ) {
        this.transaccionRepository = transaccionRepository;
        this.productoRepository = productoRepository;
        this.transaccionMapper = transaccionMapper;
        this.procesadoresPorTipo = new EnumMap<>(TipoTransaccion.class);
        procesadores.forEach(procesador -> procesadoresPorTipo.put(procesador.getTipo(), procesador));
    }

    @Override
    @Transactional
    public TransaccionResponse crear(TransaccionCreateRequest request) {
        ProcesadorTransaccion procesador = procesadoresPorTipo.get(request.getTipoTransaccion());
        if (procesador == null) {
            throw new BusinessRuleException("Tipo de transacción no soportado: " + request.getTipoTransaccion());
        }

        Transaccion transaccion = procesador.procesar(request);
        return transaccionMapper.toResponse(transaccionRepository.save(transaccion));
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionResponse obtenerPorId(Long id) {
        return transaccionMapper.toResponse(buscarTransaccionOLanzarExcepcion(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> obtenerTodas() {
        return transaccionRepository.findAll().stream()
            .map(transaccionMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> obtenerPorProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("No existe un producto con id: " + productoId);
        }
        return transaccionRepository
            .findByProductoOrigenIdOrProductoDestinoIdOrderByFechaCreacionDesc(productoId, productoId)
            .stream()
            .map(transaccionMapper::toResponse)
            .toList();
    }

    private Transaccion buscarTransaccionOLanzarExcepcion(Long id) {
        return transaccionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con id: " + id));
    }
}
