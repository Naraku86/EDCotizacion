package com.edcotizacion.prueba;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.edcotizacion.PruebaIntegracion;
import com.edcotizacion.cotizacion.CotizacionService;

@SpringBootTest(properties = "app.prueba.activa=true")
@AutoConfigureMockMvc
class InstanciaPruebaIT extends PruebaIntegracion {

    @Autowired MockMvc mvc;
    @Autowired CotizacionService cotizaciones;
    @Autowired DatosEjemplo datosEjemplo;

    private MockHttpSession entrar() throws Exception {
        return (MockHttpSession) mvc.perform(post("/login").param("username", "admin").param("password", "admin")
                        .with(csrf()))
                .andExpect(authenticated()).andExpect(redirectedUrl("/"))
                .andReturn().getRequest().getSession();
    }

    @Test
    void entraConAdminSinCambioObligatorioYVeElAvisoYLosEjemplos() throws Exception {
        mvc.perform(get("/login")).andExpect(content().string(containsString("class=\"login-prueba\"")))
                .andExpect(content().string(containsString("contraseña <b>admin</b>")));
        MockHttpSession sesion = entrar();
        mvc.perform(get("/").session(sesion)).andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"aviso-prueba\"")))
                .andExpect(content().string(containsString("se borran cada 8 horas")))
                .andExpect(content().string(not(containsString("Cámbialos aquí"))))
                .andExpect(content().string(containsString("Escuela Primaria Benito Juárez")))
                .andExpect(content().string(containsString("Taller Hernández")));
    }

    @Test
    void losEjemplosSoloSeCarganConLaBaseVacia() throws Exception {
        int antes = cotizaciones.buscar(null, null).size();
        assertEquals(4, antes);
        datosEjemplo.run(null);
        assertEquals(antes, cotizaciones.buscar(null, null).size());
    }

    @Test
    void laCuentaNoSePuedeCambiar() throws Exception {
        MockHttpSession sesion = entrar();
        mvc.perform(get("/cuenta").session(sesion)).andExpect(status().isOk())
                .andExpect(content().string(containsString("no se")))
                .andExpect(content().string(not(containsString("name=\"nuevo\""))));
        // aunque alguien mande el formulario a mano, el servicio lo rechaza
        mvc.perform(post("/cuenta").session(sesion).with(csrf()).param("actual", "admin").param("nombre", "otro")
                        .param("nuevo", "clave-nueva-123").param("confirmar", "clave-nueva-123"))
                .andExpect(redirectedUrl("/cuenta"))
                .andExpect(flash().attribute("error", "En la instancia de prueba no se puede cambiar el usuario ni la contraseña."));
        entrar(); // admin / admin sigue funcionando
    }
}
