package com.edcotizacion.seguridad;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByPorDefectoTrue();
}
