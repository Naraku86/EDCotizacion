package com.edcotizacion.demo;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.edcotizacion.PruebaIntegracion;

@SpringBootTest(properties = "app.demo.activo=false")
@AutoConfigureMockMvc
class DemoApagadoIT extends PruebaIntegracion {

    @Autowired MockMvc mvc;

    @Test
    void apagadoPideSesionYNoExiste() throws Exception {
        mvc.perform(get("/demo")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/demo.js")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/demo").with(user("admin"))).andExpect(status().isNotFound());
        mvc.perform(get("/login")).andExpect(content().string(not(containsString("/demo"))));
    }
}
