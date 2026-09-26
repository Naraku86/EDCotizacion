package com.edcotizacion.escritorio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class InstanciaUnicaTest {

    private static HttpServer servidor(String cuerpo) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        s.createContext("/login", ex -> {
            byte[] b = cuerpo.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        s.start();
        return s;
    }

    @Test
    void reconoceLaPantallaDeEntradaDeEstaApp() throws Exception {
        HttpServer s = servidor("<html><title>Entrar · Cotizaciones</title></html>");
        try {
            assertTrue(InstanciaUnica.yaAbierta(s.getAddress().getPort()));
        } finally {
            s.stop(0);
        }
    }

    @Test
    void otroProgramaEnElPuertoNoCuenta() throws Exception {
        HttpServer s = servidor("<html><title>Otra cosa</title></html>");
        try {
            assertFalse(InstanciaUnica.yaAbierta(s.getAddress().getPort()));
        } finally {
            s.stop(0);
        }
    }

    @Test
    void puertoLibre() throws Exception {
        int libre;
        try (ServerSocket ss = new ServerSocket(0)) {
            libre = ss.getLocalPort();
        }
        assertFalse(InstanciaUnica.yaAbierta(libre));
    }
}
