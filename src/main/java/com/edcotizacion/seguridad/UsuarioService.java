package com.edcotizacion.seguridad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
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

    private final JdbcClient jdbc;
    private final PasswordEncoder encoder;

    public UsuarioService(JdbcClient jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (jdbc.sql("SELECT COUNT(*) FROM usuario").query(Integer.class).single() == 0) {
            jdbc.sql("INSERT INTO usuario (nombre, password, por_defecto) VALUES (?, ?, 1)")
                    .params(USUARIO_DEFAULT, encoder.encode(PASSWORD_DEFAULT)).update();
            log.info("Usuario inicial creado: {} / {} (cámbialo en Cuenta)", USUARIO_DEFAULT, PASSWORD_DEFAULT);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String nombre) {
        return jdbc.sql("SELECT nombre, password FROM usuario WHERE nombre = ?")
                .param(nombre)
                .query((rs, i) -> User.withUsername(rs.getString(1)).password(rs.getString(2)).build())
                .optional()
                .orElseThrow(() -> new UsernameNotFoundException(nombre));
    }

    /** true mientras alguien siga entrando con la contraseña de fábrica. */
    public boolean hayPasswordDefault() {
        return jdbc.sql("SELECT COUNT(*) FROM usuario WHERE por_defecto = 1").query(Integer.class).single() > 0;
    }

    /** Cambia usuario y contraseña. Lanza IllegalArgumentException con un mensaje para mostrar. */
    @Transactional
    public void cambiar(String actual, String passwordActual, String nuevoNombre, String nuevoPassword) {
        String hash = jdbc.sql("SELECT password FROM usuario WHERE nombre = ?").param(actual)
                .query(String.class).optional().orElse(null);
        if (hash == null || !encoder.matches(passwordActual, hash)) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        nuevoNombre = nuevoNombre == null ? "" : nuevoNombre.trim();
        if (nuevoNombre.isEmpty()) {
            throw new IllegalArgumentException("El usuario no puede quedar vacío.");
        }
        if (nuevoPassword == null || nuevoPassword.length() < 4) {
            throw new IllegalArgumentException("La contraseña nueva debe tener al menos 4 caracteres.");
        }
        if (!nuevoNombre.equalsIgnoreCase(actual) && jdbc.sql("SELECT COUNT(*) FROM usuario WHERE nombre = ?")
                .param(nuevoNombre).query(Integer.class).single() > 0) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }
        jdbc.sql("UPDATE usuario SET nombre = ?, password = ?, por_defecto = ? WHERE nombre = ?")
                .params(nuevoNombre, encoder.encode(nuevoPassword),
                        USUARIO_DEFAULT.equalsIgnoreCase(nuevoNombre) && PASSWORD_DEFAULT.equals(nuevoPassword) ? 1 : 0,
                        actual)
                .update();
    }
}
