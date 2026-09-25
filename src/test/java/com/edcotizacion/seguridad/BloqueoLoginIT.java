package com.edcotizacion.seguridad;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
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

    @Test
    void cincoIntentosFallidosBloqueanCincoMinutos() throws Exception {
        for (int i = 0; i < UsuarioService.INTENTOS_MAXIMOS; i++) {
            mvc.perform(formLogin().user("admin").password("mala")).andExpect(redirectedUrl("/login?error"));
        }
        // bloqueado: ni con la contraseña correcta
        mvc.perform(formLogin().user("admin").password("admin"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?bloqueado"));

        reloj.ahora = reloj.ahora.plus(UsuarioService.BLOQUEO).plusSeconds(1);
        mvc.perform(formLogin().user("admin").password("admin")).andExpect(authenticated());
    }
}
