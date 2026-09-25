package com.edcotizacion.demo;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.edcotizacion.PruebaIntegracion;
import com.edcotizacion.cotizacion.CotizacionForm.PartidaForm;
import com.edcotizacion.cotizacion.DatosCliente;
import com.edcotizacion.demo.DemoForm.DisenoDemo;
import com.edcotizacion.pdf.Empresa;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DemoIT extends PruebaIntegracion {

    private static final List<String> TABLAS = List.of(
            "empresa", "usuario", "config", "cliente", "producto", "cotizacion", "partida");

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;

    static DemoForm form(Empresa empresa, List<PartidaForm> partidas) {
        return new DemoForm(empresa, new DisenoDemo("moderna", "#2D3436", "#E17055", "a4"), "DEMO-7",
                LocalDate.of(2026, 9, 25), 15, new DatosCliente("Cliente del demo", null, null, null, null, null),
                true, new BigDecimal("16"), new BigDecimal("100"), "Transferencia", null, null, null, partidas);
    }

    static DemoForm form() {
        return form(new Empresa("Empresa Demo", null, "XAXX010101000", null, null, null, null, null, null),
                List.of(new PartidaForm("Servicio de prueba", new BigDecimal("2"), null, new BigDecimal("500"), null)));
    }

    private String cuerpo(DemoForm f) {
        return json.writeValueAsString(f);
    }

    /** Todo el contenido de la base, para comprobar que el demo no la toca. */
    private Map<String, List<Map<String, Object>>> base() {
        Map<String, List<Map<String, Object>>> m = new LinkedHashMap<>();
        TABLAS.forEach(t -> m.put(t, jdbc.queryForList("SELECT * FROM " + t)));
        return m;
    }

    @Test
    void laPaginaAbreSinSesion() throws Exception {
        mvc.perform(get("/demo")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Descargar PDF")))
                .andExpect(content().string(containsString("data-ejemplos=")));
        mvc.perform(get("/demo.js")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(content().string(containsString("href=\"/demo\"")));
    }

    @Test
    void generaElPdfSinSesionYSinTocarLaBase() throws Exception {
        var antes = base();
        byte[] archivo = mvc.perform(post("/demo/pdf").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(form())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("DEMO-7_Cliente_del_demo.pdf")))
                .andReturn().getResponse().getContentAsByteArray();
        try (var doc = Loader.loadPDF(archivo)) {
            String texto = new PDFTextStripper().getText(doc).replaceAll("\\s+", " ");
            assertTrue(texto.contains("Cliente del demo"), texto);
            assertTrue(texto.contains("XAXX010101000"), texto);
            assertTrue(texto.contains("1,260.00"), "1000 + 16% de IVA + 100 de envío sin IVA: " + texto);
        }
        mvc.perform(post("/demo/vista").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(cuerpo(form())))
                .andExpect(status().isOk()).andExpect(content().string(containsString("class=\"hoja")));
        assertEquals(antes, base());
    }

    @Test
    void validaLoCapturado() throws Exception {
        List<PartidaForm> muchas = Collections.nCopies(DemoForm.MAX_PARTIDAS + 1,
                new PartidaForm("Algo", BigDecimal.ONE, null, BigDecimal.TEN, null));
        mvc.perform(post("/demo/pdf").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(form(form().empresa(), muchas))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El demo admite hasta 50 productos."));

        Empresa logoFalso = new Empresa("X", null, null, null, null, null, null, null, "data:text/html;base64,PHNjcmlwdD4=");
        mvc.perform(post("/demo/pdf").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(form(logoFalso, form().partidas()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El logo debe ser una imagen PNG o JPG."));

        String colorMalo = cuerpo(form()).replace("#E17055", "red;} body{display:none");
        mvc.perform(post("/demo/vista").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(colorMalo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El color de acento no es válido."));

        mvc.perform(post("/demo/pdf").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(form(form().empresa(), List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Agrega al menos un producto."));
    }

    @Test
    void sinTokenCsrfSeRechazaYElRestoSiguePidiendoSesion() throws Exception {
        mvc.perform(post("/demo/pdf").contentType(MediaType.APPLICATION_JSON).content(cuerpo(form())))
                .andExpect(status().isForbidden());
        mvc.perform(get("/")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/configuracion")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/demo/otra-cosa")).andExpect(status().is3xxRedirection());
    }
}
