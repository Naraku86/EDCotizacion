package com.edcotizacion.empresa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmisorRepository extends JpaRepository<Emisor, Long> {

    List<Emisor> findAllByOrderByNombreAsc();

    /** La primera que se dio de alta (la que traía la instalación antes de haber varias). */
    Optional<Emisor> findFirstByOrderByIdAsc();
}
