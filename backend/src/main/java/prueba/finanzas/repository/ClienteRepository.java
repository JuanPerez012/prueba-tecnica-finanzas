package prueba.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prueba.finanzas.entity.Cliente;

import java.util.Optional;

/**
 * Capa: Repository. Spring Data JPA genera la implementación en tiempo
 * de ejecución (DML/DDL delegados a Hibernate según ddl-auto=update).
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByNumeroIdentificacionAndIdNot(String numeroIdentificacion, Long id);

    boolean existsByCorreoElectronicoAndIdNot(String correoElectronico, Long id);
}
