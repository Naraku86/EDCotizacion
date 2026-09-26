package com.edcotizacion.escritorio;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.edcotizacion.PruebaIntegracion;

@SpringBootTest(properties = "app.modo=servidor")
@AutoConfigureMockMvc
class ModoServidorIT extends PruebaIntegracion {

    @Autowired MockMvc mvc;

    @Test
    void enServidorNoSePuedeCerrarElProgramaDesdeLaWeb() throws Exception {
        mvc.perform(get("/configuracion").with(user("admin")))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("Cerrar programa"))));
        mvc.perform(post("/apagar").with(user("admin")).with(csrf())).andExpect(status().isNotFound());
    }
}
