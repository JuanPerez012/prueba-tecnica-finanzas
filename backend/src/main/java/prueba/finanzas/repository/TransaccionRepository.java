package prueba.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prueba.finanzas.entity.Transaccion;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByProductoOrigenIdOrProductoDestinoIdOrderByFechaCreacionDesc(
        Long productoOrigenId, Long productoDestinoId
    );
}
