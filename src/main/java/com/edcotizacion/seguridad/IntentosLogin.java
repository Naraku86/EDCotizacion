package com.edcotizacion.seguridad;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * Intentos fallidos de entrar, contados en memoria por usuario + IP y por IP.
 * <ul>
 * <li>{@value #POR_USUARIO} fallos de un usuario desde una IP bloquean esa combinación {@link #BLOQUEO}.
 *     Otra computadora puede seguir entrando con ese usuario: nadie puede dejar fuera al dueño
 *     de la cuenta solo con equivocarse a propósito.</li>
 * <li>{@value #POR_IP} fallos desde una IP, con cualquier usuario, bloquean esa IP.</li>
 * </ul>
 * Los usuarios que no existen se cuentan igual, así que la respuesta no revela cuáles existen.
 */
@Component
public class IntentosLogin {

    private static final Logger log = LoggerFactory.getLogger(IntentosLogin.class);

    public static final int POR_USUARIO = 5;
    public static final int POR_IP = 20;
    public static final Duration BLOQUEO = Duration.ofMinutes(5);
    private static final int LIMPIAR_DESDE = 10_000;

    private record Registro(int fallos, Instant ultimo, Instant bloqueadoHasta) {
    }

    private final Clock reloj;
    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public IntentosLogin(Clock reloj) {
        this.reloj = reloj;
    }

    public boolean bloqueado(String usuario, String ip) {
        Instant ahora = reloj.instant();
        return activo(registros.get(clave(usuario, ip)), ahora) || activo(registros.get(clave(null, ip)), ahora);
    }

    public void fallo(String usuario, String ip) {
        Instant ahora = reloj.instant();
        if (registros.size() > LIMPIAR_DESDE) {
            registros.values().removeIf(r -> vencido(r, ahora));
        }
        contar(clave(usuario, ip), POR_USUARIO, ahora);
        contar(clave(null, ip), POR_IP, ahora);
    }

    /** Al entrar bien se olvidan los fallos de ese usuario desde esa IP. */
    public void exito(String usuario, String ip) {
        registros.remove(clave(usuario, ip));
    }

    @EventListener
    public void alFallar(AuthenticationFailureBadCredentialsEvent e) {
        fallo(e.getAuthentication().getName(), ip(e.getAuthentication()));
    }

    @EventListener
    public void alEntrar(AuthenticationSuccessEvent e) {
        exito(e.getAuthentication().getName(), ip(e.getAuthentication()));
    }

    private void contar(String clave, int maximo, Instant ahora) {
        boolean[] recienBloqueado = { false };
        registros.compute(clave, (k, actual) -> {
            if (activo(actual, ahora)) {
                return actual; // un bloqueo vigente no se reinicia ni se levanta con más fallos
            }
            int fallos = actual == null || vencido(actual, ahora) ? 1 : actual.fallos() + 1;
            if (fallos >= maximo) {
                recienBloqueado[0] = true;
                return new Registro(fallos, ahora, ahora.plus(BLOQUEO));
            }
            return new Registro(fallos, ahora, null);
        });
        if (recienBloqueado[0]) {
            log.warn("Entrada bloqueada {} min para {}", BLOQUEO.toMinutes(), clave);
        }
    }

    private static boolean activo(Registro r, Instant ahora) {
        return r != null && r.bloqueadoHasta() != null && ahora.isBefore(r.bloqueadoHasta());
    }

    /** Los fallos sueltos se olvidan tras el tiempo de bloqueo; un bloqueo, cuando vence. */
    private static boolean vencido(Registro r, Instant ahora) {
        return r.bloqueadoHasta() != null ? !ahora.isBefore(r.bloqueadoHasta())
                : !ahora.isBefore(r.ultimo().plus(BLOQUEO));
    }

    private static String clave(String usuario, String ip) {
        return (usuario == null ? "*" : usuario.trim().toLowerCase(Locale.ROOT)) + "|" + ip;
    }

    private static String ip(Authentication a) {
        return a.getDetails() instanceof WebAuthenticationDetails d ? d.getRemoteAddress() : "desconocida";
    }
}
