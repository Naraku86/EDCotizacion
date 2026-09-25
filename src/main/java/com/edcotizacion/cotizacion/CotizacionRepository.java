package com.edcotizacion.cotizacion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    /** Cotización con sus partidas ya cargadas (para editar, duplicar o imprimir). */
    @EntityGraph(attributePaths = {"partidas", "empresa"})
    Optional<Cotizacion> findConPartidasById(Long id);

    /** Lista de la pantalla principal; estado o empresaId null = sin ese filtro. */
    @EntityGraph(attributePaths = "empresa")
    @Query("""
            select c from Cotizacion c
            where (:estado is null or c.estado = :estado)
              and (:empresaId is null or c.empresa.id = :empresaId)
            order by c.id desc""")
    List<Cotizacion> filtrar(Estado estado, Long empresaId);

    boolean existsByFolio(String folio);

    /** Descripciones de todas las partidas, para buscar cotizaciones por producto. */
    @Query("select new com.edcotizacion.cotizacion.TextoPartida(p.cotizacion.id, p.descripcion) from Partida p")
    List<TextoPartida> textosDePartidas();
}
