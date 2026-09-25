package com.edcotizacion.cotizacion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    /** Cotización con sus partidas ya cargadas (para editar, duplicar o imprimir). */
    @EntityGraph(attributePaths = "partidas")
    Optional<Cotizacion> findConPartidasById(Long id);

    List<Cotizacion> findAllByOrderByIdDesc();

    List<Cotizacion> findByEstadoOrderByIdDesc(Estado estado);

    boolean existsByFolio(String folio);

    /** Descripciones de todas las partidas, para buscar cotizaciones por producto. */
    @Query("select new com.edcotizacion.cotizacion.TextoPartida(p.cotizacion.id, p.descripcion) from Partida p")
    List<TextoPartida> textosDePartidas();
}
