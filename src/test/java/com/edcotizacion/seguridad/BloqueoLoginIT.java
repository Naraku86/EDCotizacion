package com.edcotizacion.seguridad;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.edcotizacion.PruebaIntegracion;

@SpringBootTest
@AutoConfigureMockMvc
class BloqueoLoginIT extends PruebaIntegracion {

    /** Reloj que se puede adelantar a mano. */
    static class RelojDePrueba extends Clock {
        Instant ahora = Instant.parse("2026-09-25T12:00:00Z");

        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        RelojDePrueba relojDePrueba() {
            return new RelojDePrueba();
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private RelojDePrueba reloj;

    private static MockHttpServletRequestBuilder entrar(String usuario, String password, String ip) {
        return post("/login").param("username", usuario).param("password", password).with(csrf())
                .with(r -> { r.setRemoteAddr(ip); return r; });
    }

    @Test
    void cincoFallosBloqueanEseUsuarioSoloDesdeEsaIpYElMensajeNoCambia() throws Exception {
        for (int i = 0; i < IntentosLogin.POR_USUARIO; i++) {
            mvc.perform(entrar("admin", "mala", "10.0.0.66")).andExpect(redirectedUrl("/login?error"));
        }
        // desde esa IP, ni con la contraseña correcta, y con el mismo mensaje que un error normal
        mvc.perform(entrar("admin", "admin", "10.0.0.66"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
        // el dueño, desde otra computadora, sí puede entrar
        mvc.perform(entrar("admin", "admin", "10.0.0.7")).andExpect(authenticated());

        reloj.ahora = reloj.ahora.plus(IntentosLogin.BLOQUEO).plusSeconds(1);
        mvc.perform(entrar("admin", "admin", "10.0.0.66")).andExpect(authenticated());
    }

    @Test
    void unUsuarioQueNoExisteSeComportaIgualQueUnoQueSi() throws Exception {
        for (int i = 0; i <= IntentosLogin.POR_USUARIO; i++) {
            mvc.perform(entrar("fantasma", "x", "10.0.0.88")).andExpect(redirectedUrl("/login?error"));
        }
    }

    @Test
    void muchosFallosDesdeUnaIpBloqueanEsaIpConCualquierUsuario() throws Exception {
        for (int i = 0; i < IntentosLogin.POR_IP; i++) {
            mvc.perform(entrar("usuario" + i, "x", "10.0.0.99")).andExpect(redirectedUrl("/login?error"));
        }
        mvc.perform(entrar("admin", "admin", "10.0.0.99")).andExpect(unauthenticated());
        mvc.perform(entrar("admin", "admin", "10.0.0.100")).andExpect(authenticated());
    }
}
