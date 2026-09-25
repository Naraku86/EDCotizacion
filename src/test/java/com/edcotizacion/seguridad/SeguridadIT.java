package com.edcotizacion.seguridad;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.edcotizacion.PruebaIntegracion;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadIT extends PruebaIntegracion {

    private static final String COTIZACION_INVALIDA = """
            {"fecha":"2026-09-25","vigenciaDias":15,"cliente":{"nombre":"X"},"aplicaIva":true,"tasaIva":16,
             "partidas":[{"descripcion":"Laptop","cantidad":0,"precioUnitario":100}],
             "folio":"HACKEO","estado":"ACEPTADA"}""";

    @Autowired
    private MockMvc mvc;

    @Test
    void sinSesionTodoLlevaAlLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/api/clientes").param("q", "a")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/configuracion/plantilla/muestra.pdf")).andExpect(status().is3xxRedirection());
    }

    @Test
    void loginConUsuarioDeFabricaYConContrasenaIncorrecta() throws Exception {
        mvc.perform(formLogin().user("admin").password("admin")).andExpect(authenticated());
        mvc.perform(formLogin().user("admin").password("otra")).andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
        mvc.perform(formLogin().user("nadie").password("x")).andExpect(unauthenticated());
    }

    @Test
    void postSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/api/cotizaciones").with(user("admin"))
                .contentType(MediaType.APPLICATION_JSON).content(COTIZACION_INVALIDA))
                .andExpect(status().isForbidden());
    }

    @Test
    void validacionDevuelveMensajesParaElUsuario() throws Exception {
        mvc.perform(post("/api/cotizaciones").with(user("admin")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(COTIZACION_INVALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Partida 1: la cantidad debe ser mayor a 0."));
    }

    @Test
    void jsonMalFormadoNoMuestraDetallesInternos() throws Exception {
        mvc.perform(post("/api/cotizaciones").with(user("admin")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"fecha\": \"no-es-fecha\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Los datos enviados no tienen el formato esperado."));
    }

    @Test
    void cabecerasDeSeguridad() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self';")))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void peticionEnormeSeRechazaAntesDeLeerla() throws Exception {
        mvc.perform(post("/api/cotizaciones").with(user("admin")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(new byte[6 * 1024 * 1024]))
                .andExpect(status().isContentTooLarge());
    }

    @Test
    void logoQueNoEsPngNiJpgSeRechaza() throws Exception {
        mvc.perform(post("/configuracion/plantilla").with(user("admin")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"diseno\":{},\"empresa\":{\"logo\":\"data:image/svg+xml;base64,PHN2Zz4=\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El logo debe ser una imagen PNG o JPG."));
    }
}
