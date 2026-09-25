package com.edcotizacion.demo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.edcotizacion.demo.LimiteDemo.LimiteExcedidoException;

class LimiteDemoTest {

    /** Reloj que solo avanza cuando la prueba lo pide. */
    private static final class Reloj extends Clock {
        private Instant ahora = Instant.parse("2026-09-25T10:00:00Z");
        void avanzar(Duration d) { ahora = ahora.plus(d); }
        @Override public Instant instant() { return ahora; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zona) { return this; }
    }

    @Test
    void cuentaPorIpYSeReiniciaAlMinuto() {
        Reloj reloj = new Reloj();
        LimiteDemo limite = new LimiteDemo(100, 2, 1, reloj);
        limite.pdf("10.0.0.1");
        limite.pdf("10.0.0.1");
        LimiteExcedidoException e = assertThrows(LimiteExcedidoException.class, () -> limite.pdf("10.0.0.1"));
        assertEquals("Generaste demasiados PDF seguidos. Espera un minuto y vuelve a intentar.", e.getMessage());
        assertDoesNotThrow(() -> limite.pdf("10.0.0.2"), "otra IP tiene su propio contador");
        assertDoesNotThrow(() -> limite.vista("10.0.0.1"), "las vistas previas se cuentan aparte");
        reloj.avanzar(Duration.ofSeconds(59));
        assertThrows(LimiteExcedidoException.class, () -> limite.pdf("10.0.0.1"));
        reloj.avanzar(Duration.ofSeconds(1));
        assertDoesNotThrow(() -> limite.pdf("10.0.0.1"));
    }

    @Test
    void generarLiberaElLugarAunqueLaTareaFalle() throws Exception {
        LimiteDemo limite = new LimiteDemo(100, 100, 1, Clock.systemUTC());
        assertThrows(IllegalStateException.class, () -> limite.generar(() -> { throw new IllegalStateException("x"); }));
        assertEquals("ok", limite.generar(() -> "ok"));
    }
}
