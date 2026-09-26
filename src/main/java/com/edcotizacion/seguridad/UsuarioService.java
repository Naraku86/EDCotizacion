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

import com.edcotizacion.prueba.InstanciaPrueba;

/**
 * Usuarios de la app. La primera vez crea admin/admin, y quien entra con esa contraseña debe
 * cambiarla antes de usar la app (CambioObligatorio). Los intentos fallidos los cuenta IntentosLogin.
 */
@Service
public class UsuarioService implements UserDetailsService, ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    public static final String USUARIO_DEFAULT = "admin";
    public static final String PASSWORD_DEFAULT = "admin";
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final InstanciaPrueba prueba;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder, InstanciaPrueba prueba) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.prueba = prueba;
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
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    /** true si ese usuario sigue con la contraseña de fábrica. */
    @Transactional(readOnly = true)
    public boolean usaPasswordDefault(String nombre) {
        return usuarios.findByNombre(nombre).map(Usuario::isPorDefecto).orElse(false);
    }

    /** true mientras alguien siga entrando con la contraseña de fábrica. */
    @Transactional(readOnly = true)
    public boolean hayPasswordDefault() {
        return usuarios.existsByPorDefectoTrue();
    }

    /** Cambia usuario y contraseña (ya validados en forma). Lanza IllegalArgumentException con un mensaje para mostrar. */
    @Transactional
    public void cambiar(String actual, CambioCuenta cambio) {
        if (prueba.activa()) {
            throw new IllegalArgumentException("En la instancia de prueba no se puede cambiar el usuario ni la contraseña.");
        }
        Usuario u = usuarios.findByNombre(actual).orElse(null);
        if (u == null || !encoder.matches(cambio.actual(), u.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (!cambio.nombre().equalsIgnoreCase(actual) && usuarios.existsByNombre(cambio.nombre())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }
        u.cambiar(cambio.nombre(), encoder.encode(cambio.nuevo()));
    }
}
