package prueba.finanzas.service;

import prueba.finanzas.dto.cliente.ClienteCreateRequest;
import prueba.finanzas.dto.cliente.ClienteResponse;
import prueba.finanzas.dto.cliente.ClienteUpdateRequest;
import prueba.finanzas.entity.Cliente;
import prueba.finanzas.mapper.ClienteMapper;
import prueba.finanzas.port.ProductoConsultaPort;
import prueba.finanzas.exception.BusinessRuleException;
import prueba.finanzas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prueba.finanzas.repository.ClienteRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Capa: Service. Contiene las reglas de negocio de Cliente.
 * Las transacciones aseguran atomicidad (propiedad "A" de ACID) para
 * cada operación de escritura.
 */
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private static final int EDAD_MINIMA = 18;

    private final ClienteRepository clienteRepository;
    private final ProductoConsultaPort productoConsultaPort;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteResponse crear(ClienteCreateRequest request) {
        validarMayorEdad(request.getFechaNacimiento());
        validarUnicidadParaCreacion(request.getNumeroIdentificacion(), request.getCorreoElectronico());

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente guardado = clienteRepository.save(cliente);
        return clienteMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        return clienteMapper.toResponse(buscarClienteOLanzarExcepcion(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerTodos() {
        return clienteRepository.findAll().stream()
            .map(clienteMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteUpdateRequest request) {
        Cliente cliente = buscarClienteOLanzarExcepcion(id);

        validarMayorEdad(request.getFechaNacimiento());
        validarUnicidadParaActualizacion(id, request.getNumeroIdentificacion(), request.getCorreoElectronico());

        clienteMapper.actualizarEntity(cliente, request);
        Cliente actualizado = clienteRepository.save(cliente);
        return clienteMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscarClienteOLanzarExcepcion(id);

        if (productoConsultaPort.tieneProductosAsociados(cliente.getId())) {
            throw new BusinessRuleException(
                "No se puede eliminar el cliente porque tiene productos financieros asociados"
            );
        }

        clienteRepository.delete(cliente);
    }

    // ---- Helpers privados de dominio ----

    private Cliente buscarClienteOLanzarExcepcion(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    private void validarMayorEdad(LocalDate fechaNacimiento) {
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < EDAD_MINIMA) {
            throw new BusinessRuleException("El cliente debe ser mayor de edad (" + EDAD_MINIMA + " años o más)");
        }
    }

    private void validarUnicidadParaCreacion(String numeroIdentificacion, String correoElectronico) {
        if (clienteRepository.existsByNumeroIdentificacion(numeroIdentificacion)) {
            throw new BusinessRuleException("Ya existe un cliente con el número de identificación proporcionado");
        }
        if (clienteRepository.existsByCorreoElectronico(correoElectronico)) {
            throw new BusinessRuleException("Ya existe un cliente con el correo electrónico proporcionado");
        }
    }

    private void validarUnicidadParaActualizacion(Long id, String numeroIdentificacion, String correoElectronico) {
        if (clienteRepository.existsByNumeroIdentificacionAndIdNot(numeroIdentificacion, id)) {
            throw new BusinessRuleException("Ya existe otro cliente con el número de identificación proporcionado");
        }
        if (clienteRepository.existsByCorreoElectronicoAndIdNot(correoElectronico, id)) {
            throw new BusinessRuleException("Ya existe otro cliente con el correo electrónico proporcionado");
        }
    }
}
