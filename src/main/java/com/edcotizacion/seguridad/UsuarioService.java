package com.edcotizacion.seguridad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Usuarios de la app (tabla usuario). La primera vez crea admin/admin. */
@Service
public class UsuarioService implements UserDetailsService, ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    public static final String USUARIO_DEFAULT = "admin";
    public static final String PASSWORD_DEFAULT = "admin";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.count() == 0) {
            usuarios.save(new Usuario(USUARIO_DEFAULT, encoder.encode(PASSWORD_DEFAULT), true));
            log.info("Usuario inicial creado: {} / {} (cámbialo en Cuenta)", USUARIO_DEFAULT, PASSWORD_DEFAULT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nombre) {
        return usuarios.findByNombre(nombre)
                .map(u -> User.withUsername(u.getNombre()).password(u.getPassword()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    /** true mientras alguien siga entrando con la contraseña de fábrica. */
    @Transactional(readOnly = true)
    public boolean hayPasswordDefault() {
        return usuarios.existsByPorDefectoTrue();
    }

    /** Cambia usuario y contraseña. Lanza IllegalArgumentException con un mensaje para mostrar. */
    @Transactional
    public void cambiar(String actual, String passwordActual, String nuevoNombre, String nuevoPassword) {
        Usuario u = usuarios.findByNombre(actual).orElse(null);
        if (u == null || !encoder.matches(passwordActual, u.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        nuevoNombre = nuevoNombre == null ? "" : nuevoNombre.strip();
        if (nuevoNombre.isEmpty()) {
            throw new IllegalArgumentException("El usuario no puede quedar vacío.");
        }
        if (nuevoPassword == null || nuevoPassword.length() < 4) {
            throw new IllegalArgumentException("La contraseña nueva debe tener al menos 4 caracteres.");
        }
        if (!nuevoNombre.equalsIgnoreCase(actual) && usuarios.existsByNombre(nuevoNombre)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }
        boolean deFabrica = USUARIO_DEFAULT.equalsIgnoreCase(nuevoNombre) && PASSWORD_DEFAULT.equals(nuevoPassword);
        u.cambiar(nuevoNombre, encoder.encode(nuevoPassword), deFabrica);
    }
}
