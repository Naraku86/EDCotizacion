package com.edcotizacion.seguridad;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Usuarios de la app. La primera vez crea admin/admin; tras {@value #INTENTOS_MAXIMOS} intentos
 * fallidos seguidos la cuenta se bloquea {@link #BLOQUEO}.
 */
@Service
public class UsuarioService implements UserDetailsService, ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    public static final String USUARIO_DEFAULT = "admin";
    public static final String PASSWORD_DEFAULT = "admin";
    public static final int INTENTOS_MAXIMOS = 5;
    public static final Duration BLOQUEO = Duration.ofMinutes(5);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final Clock reloj;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder, Clock reloj) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.reloj = reloj;
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
                .map(u -> User.withUsername(u.getNombre())
                        .password(u.getPassword())
                        .accountLocked(u.estaBloqueado(ahora()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @EventListener
    @Transactional
    public void alFallar(AuthenticationFailureBadCredentialsEvent e) {
        usuarios.findByNombre(e.getAuthentication().getName()).ifPresent(u -> {
            u.registrarFallo(ahora(), INTENTOS_MAXIMOS, BLOQUEO);
            if (u.estaBloqueado(ahora())) {
                log.warn("Usuario '{}' bloqueado {} min por intentos fallidos", u.getNombre(), BLOQUEO.toMinutes());
            }
        });
    }

    @EventListener
    @Transactional
    public void alEntrar(AuthenticationSuccessEvent e) {
        usuarios.findByNombre(e.getAuthentication().getName()).ifPresent(Usuario::registrarExito);
    }

    /** true mientras alguien siga entrando con la contraseña de fábrica. */
    @Transactional(readOnly = true)
    public boolean hayPasswordDefault() {
        return usuarios.existsByPorDefectoTrue();
    }

    /** Cambia usuario y contraseña (ya validados en forma). Lanza IllegalArgumentException con un mensaje para mostrar. */
    @Transactional
    public void cambiar(String actual, CambioCuenta cambio) {
        Usuario u = usuarios.findByNombre(actual).orElse(null);
        if (u == null || !encoder.matches(cambio.actual(), u.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (!cambio.nombre().equalsIgnoreCase(actual) && usuarios.existsByNombre(cambio.nombre())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }
        u.cambiar(cambio.nombre(), encoder.encode(cambio.nuevo()));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }
}
