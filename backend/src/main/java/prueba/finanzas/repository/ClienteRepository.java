package prueba.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prueba.finanzas.entity.Cliente;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByNumeroIdentificacionAndIdNot(String numeroIdentificacion, Long id);

    boolean existsByCorreoElectronicoAndIdNot(String correoElectronico, Long id);
}
