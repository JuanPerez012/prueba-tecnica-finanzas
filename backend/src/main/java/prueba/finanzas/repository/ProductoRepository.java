package prueba.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prueba.finanzas.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Producto> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);
}
