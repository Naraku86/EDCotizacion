package com.edcotizacion.seguridad;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.edcotizacion.PruebaIntegracion;

@SpringBootTest
@AutoConfigureMockMvc
class CambioObligatorioIT extends PruebaIntegracion {

    @Autowired
    private MockMvc mvc;

    @Test
    void conLaContrasenaDeFabricaSoloSePuedeUsarCuentaHastaCambiarla() throws Exception {
        MockHttpSession sesion = (MockHttpSession) mvc.perform(post("/login")
                        .param("username", "admin").param("password", "admin").with(csrf()))
                .andExpect(authenticated()).andExpect(redirectedUrl("/cuenta"))
                .andReturn().getRequest().getSession();

        mvc.perform(get("/").session(sesion)).andExpect(redirectedUrl("/cuenta"));
        mvc.perform(get("/configuracion").session(sesion)).andExpect(redirectedUrl("/cuenta"));
        mvc.perform(post("/configuracion/empresas").param("nombre", "X").session(sesion).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/cuenta").session(sesion)).andExpect(status().isOk())
                .andExpect(content().string(containsString("contraseña de fábrica")));
        mvc.perform(get("/app.css").session(sesion)).andExpect(status().isOk());

        mvc.perform(post("/cuenta").session(sesion).with(csrf()).param("actual", "admin").param("nombre", "admin")
                        .param("nuevo", "una-clave-larga").param("confirmar", "una-clave-larga"))
                .andExpect(redirectedUrl("/login?cambio"));

        // con la contraseña nueva se entra normal
        MockHttpSession nueva = (MockHttpSession) mvc.perform(post("/login")
                        .param("username", "admin").param("password", "una-clave-larga").with(csrf()))
                .andExpect(authenticated()).andExpect(redirectedUrl("/"))
                .andReturn().getRequest().getSession();
        mvc.perform(get("/").session(nueva)).andExpect(status().isOk());
    }
}
