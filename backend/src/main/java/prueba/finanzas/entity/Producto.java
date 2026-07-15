package prueba.finanzas.entity;

import jakarta.persistence.*;
import lombok.*;
import prueba.finanzas.enums.EstadoCuenta;
import prueba.finanzas.enums.TipoCuenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "productos",
    uniqueConstraints = @UniqueConstraint(name = "uk_producto_numero_cuenta", columnNames = "numero_cuenta")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private TipoCuenta tipoCuenta;

    @Column(name = "numero_cuenta", nullable = false, length = 10)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCuenta estado;

    @Column(name = "saldo", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Column(name = "exenta_gmf", nullable = false)
    private boolean exentaGmf;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

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
