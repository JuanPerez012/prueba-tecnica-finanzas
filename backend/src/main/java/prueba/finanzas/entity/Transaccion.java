package prueba.finanzas.entity;

import jakarta.persistence.*;
import lombok.*;
import prueba.finanzas.enums.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transacciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transaccion", nullable = false, length = 20)
    private TipoTransaccion tipoTransaccion;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "producto_origen_id")
    private Producto productoOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "producto_destino_id")
    private Producto productoDestino;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Builder.Default
    @OneToMany(mappedBy = "transaccion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Movimiento> movimientos = new ArrayList<>();

    @PrePersist
    protected void alPersistir() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public void agregarMovimiento(Movimiento movimiento) {
        movimiento.setTransaccion(this);
        this.movimientos.add(movimiento);
    }
}
