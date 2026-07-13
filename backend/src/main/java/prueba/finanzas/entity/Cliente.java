package prueba.finanzas.entity;

import jakarta.persistence.*;
import lombok.*;
import prueba.finanzas.enums.TipoIdentificacion;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad Cliente.
 *
 * Capa: Entity (persistencia). No contiene lógica de negocio salvo el
 * cálculo automático de fechaCreacion / fechaModificacion, que es una
 * responsabilidad propia del ciclo de vida de la entidad (JPA lifecycle
 * callbacks), no una regla de negocio de dominio.
 */
@Entity
@Table(
    name = "clientes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_numero_identificacion", columnNames = "numero_identificacion"),
        @UniqueConstraint(name = "uk_cliente_correo_electronico", columnNames = "correo_electronico")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_identificacion", nullable = false, length = 20)
    private TipoIdentificacion tipoIdentificacion;

    @Column(name = "numero_identificacion", nullable = false, length = 30)
    private String numeroIdentificacion;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "correo_electronico", nullable = false, length = 150)
    private String correoElectronico;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void alPersistir() {
        LocalDateTime ahora = LocalDateTime.now();
        this.fechaCreacion = ahora;
        this.fechaModificacion = ahora;
    }

    @PreUpdate
    protected void alActualizar() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
