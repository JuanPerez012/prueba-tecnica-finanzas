package prueba.finanzas.mapper;

import prueba.finanzas.entity.Cliente;
import prueba.finanzas.dto.cliente.ClienteCreateRequest;
import prueba.finanzas.dto.cliente.ClienteResponse;
import prueba.finanzas.dto.cliente.ClienteUpdateRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper manual (sin librerías externas) entre la entidad Cliente y sus DTOs.
 * Mantenerlo explícito facilita el control sobre qué campos se exponen y
 * evita que datos sensibles o de auditoría lleguen desde el cliente HTTP.
 */
@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteCreateRequest request) {
        return Cliente.builder()
            .tipoIdentificacion(request.getTipoIdentificacion())
            .numeroIdentificacion(request.getNumeroIdentificacion())
            .nombres(request.getNombres())
            .apellidos(request.getApellidos())
            .correoElectronico(request.getCorreoElectronico())
            .fechaNacimiento(request.getFechaNacimiento())
            .build();
    }

    public void actualizarEntity(Cliente cliente, ClienteUpdateRequest request) {
        cliente.setTipoIdentificacion(request.getTipoIdentificacion());
        cliente.setNumeroIdentificacion(request.getNumeroIdentificacion());
        cliente.setNombres(request.getNombres());
        cliente.setApellidos(request.getApellidos());
        cliente.setCorreoElectronico(request.getCorreoElectronico());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        // fechaCreacion no se toca; fechaModificacion se recalcula en @PreUpdate
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
            .id(cliente.getId())
            .tipoIdentificacion(cliente.getTipoIdentificacion())
            .numeroIdentificacion(cliente.getNumeroIdentificacion())
            .nombres(cliente.getNombres())
            .apellidos(cliente.getApellidos())
            .correoElectronico(cliente.getCorreoElectronico())
            .fechaNacimiento(cliente.getFechaNacimiento())
            .fechaCreacion(cliente.getFechaCreacion())
            .fechaModificacion(cliente.getFechaModificacion())
            .build();
    }
}
