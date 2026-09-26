package com.edcotizacion.seguridad;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class IntentosLoginTest {

    private static final class Reloj extends Clock {
        Instant ahora = Instant.parse("2026-09-25T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    @Test
    void unBloqueoVigenteNoSeLevantaConMasFallos() {
        Reloj reloj = new Reloj();
        IntentosLogin intentos = new IntentosLogin(reloj);
        for (int i = 0; i < IntentosLogin.POR_IP; i++) {
            intentos.fallo("u" + i, "1.1.1.1");
        }
        assertTrue(intentos.bloqueado("otro", "1.1.1.1"));
        intentos.fallo("otro", "1.1.1.1");
        assertTrue(intentos.bloqueado("otro", "1.1.1.1"), "seguir fallando no reinicia el conteo");
        reloj.ahora = reloj.ahora.plus(IntentosLogin.BLOQUEO);
        assertFalse(intentos.bloqueado("otro", "1.1.1.1"));
    }

    @Test
    void mayusculasYEspaciosCuentanComoElMismoUsuario() {
        IntentosLogin intentos = new IntentosLogin(new Reloj());
        for (int i = 0; i < IntentosLogin.POR_USUARIO; i++) {
            intentos.fallo(i % 2 == 0 ? "Admin" : " admin ", "2.2.2.2");
        }
        assertTrue(intentos.bloqueado("ADMIN", "2.2.2.2"));
        assertFalse(intentos.bloqueado("admin", "2.2.2.3"));
    }

    @Test
    void fallosSueltosSeOlvidanConElTiempo() {
        Reloj reloj = new Reloj();
        IntentosLogin intentos = new IntentosLogin(reloj);
        for (int i = 0; i < IntentosLogin.POR_USUARIO - 1; i++) {
            intentos.fallo("admin", "3.3.3.3");
        }
        reloj.ahora = reloj.ahora.plus(IntentosLogin.BLOQUEO);
        intentos.fallo("admin", "3.3.3.3");
        assertFalse(intentos.bloqueado("admin", "3.3.3.3"));
    }
}
