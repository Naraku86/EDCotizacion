package com.edcotizacion.demo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.edcotizacion.PruebaIntegracion;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(properties = "app.demo.pdf-por-minuto=2")
@AutoConfigureMockMvc
class DemoLimiteIT extends PruebaIntegracion {

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;

    private static RequestPostProcessor ip(String ip) {
        return r -> { r.setRemoteAddr(ip); return r; };
    }

    @Test
    void despuesDelLimiteResponde429ConMensaje() throws Exception {
        String cuerpo = json.writeValueAsString(DemoIT.form());
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/demo/pdf").with(csrf()).with(ip("10.1.1.1"))
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isOk());
        }
        mvc.perform(post("/demo/pdf").with(csrf()).with(ip("10.1.1.1"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.error").value("Generaste demasiados PDF seguidos. Espera un minuto y vuelve a intentar."));
        mvc.perform(post("/demo/pdf").with(csrf()).with(ip("10.1.1.2"))
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andExpect(status().isOk());
    }
}
