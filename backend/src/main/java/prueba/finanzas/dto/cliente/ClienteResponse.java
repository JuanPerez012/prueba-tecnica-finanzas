package prueba.finanzas.dto.cliente;

import prueba.finanzas.enums.TipoIdentificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ClienteResponse {

    private Long id;
    private TipoIdentificacion tipoIdentificacion;
    private String numeroIdentificacion;
    private String nombres;
    private String apellidos;
    private String correoElectronico;
    private LocalDate fechaNacimiento;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
