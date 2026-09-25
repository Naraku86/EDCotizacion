package com.edcotizacion.empresa;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.edcotizacion.PruebaIntegracion;
import com.edcotizacion.cotizacion.*;
import com.edcotizacion.pdf.*;
import com.edcotizacion.comun.NoEncontradoException;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EmpresasIT extends PruebaIntegracion {
    @Autowired DisenoService disenos;
    @Autowired EmisorService emisores;
    @Autowired CotizacionService cotizaciones;
    @Autowired PdfService pdf;
    @Autowired JsonMapper json;
    @Autowired MockMvc mvc;

    private CotizacionForm form(Long empresaId) {
        return new CotizacionForm(empresaId, LocalDate.now(), 15,
                new DatosCliente("Cliente multiempresa", null, null, null, null, null),
                true, new BigDecimal("16"), BigDecimal.ZERO, null, null, null, null,
                List.of(new CotizacionForm.PartidaForm("Servicio", BigDecimal.ONE, null, new BigDecimal("100"), null)));
    }

    private Empresa datos(String nombre) {
        return new Empresa(nombre, null, null, null, null, null, null, null, null);
    }

    private String textoPdf(long id) throws Exception {
        try (var doc = Loader.loadPDF(pdf.generar(cotizaciones.obtener(id)))) {
            return new PDFTextStripper().getText(doc).replaceAll("\\s+", " ");
        }
    }

    @Test
    void cadaPdfUsaSuEmpresaYPlantillaInclusoDespuesDeEditarOtra() throws Exception {
        long a = emisores.crear("Empresa Alfa");
        long b = emisores.crear("Empresa Beta");
        disenos.guardar(a, Map.of("textos", Map.of("titulo", "PROPUESTA ALFA")), datos("Empresa Alfa"));
        disenos.guardar(b, Map.of("plantilla", "minimalista", "textos", Map.of("titulo", "PROPUESTA BETA")), datos("Empresa Beta"));
        long ca = cotizaciones.guardar(null, form(a));
        long cb = cotizaciones.guardar(null, form(b));
        assertTrue(textoPdf(ca).contains("PROPUESTA ALFA"));
        assertTrue(textoPdf(cb).contains("PROPUESTA BETA"));
        assertFalse(textoPdf(ca).contains("BETA"));
        disenos.guardar(b, Map.of("textos", Map.of("titulo", "BETA NUEVA")), datos("Empresa Beta renovada"));
        assertTrue(textoPdf(ca).contains("PROPUESTA ALFA"));
        assertFalse(textoPdf(ca).contains("BETA"));
        assertTrue(textoPdf(cb).contains("BETA NUEVA"));
        assertEquals("Empresa Alfa", cotizaciones.obtener(ca).getEmpresa().getNombre());
    }

    @Test
    void editarYDuplicarConservanLaEmpresaYPermitenElegirOtra() {
        long a = emisores.crear("Emisor de copia");
        long b = emisores.crear("Emisor alternativo");
        long original = cotizaciones.guardar(null, form(a));
        long copia = cotizaciones.duplicar(original);
        assertEquals(a, cotizaciones.obtener(copia).getEmpresa().getId());
        assertNotEquals(cotizaciones.obtener(original).getFolio(), cotizaciones.obtener(copia).getFolio());
        CotizacionForm edicion = CotizacionForm.de(cotizaciones.obtener(original));
        cotizaciones.guardar(original, edicion);
        assertEquals(a, cotizaciones.obtener(original).getEmpresa().getId());
        cotizaciones.guardar(copia, form(b));
        assertEquals(b, cotizaciones.obtener(copia).getEmpresa().getId());
        assertEquals(a, cotizaciones.obtener(original).getEmpresa().getId());
    }

    @Test
    void laPredeterminadaEsLaPrimeraRegistradaAunqueOtraOrdeneAntes() {
        long primera = emisores.predeterminada().getId();
        emisores.crear("AAA se ordena primero");
        assertEquals(primera, emisores.predeterminada().getId());
        assertEquals(primera, cotizaciones.nueva().getEmpresa().getId());
        assertEquals("AAA se ordena primero", emisores.todas().get(0).getNombre());
    }

    @Test
    void validaEmpresaAntesDeGuardar() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> emisores.crear("   "));
        assertThrows(IllegalArgumentException.class, () -> emisores.crear("x".repeat(201)));
        assertThrows(NoEncontradoException.class, () -> cotizaciones.guardar(null, form(999999L)));
        mvc.perform(post("/api/cotizaciones").with(user("admin")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(form(null))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Elige la empresa emisora."));
        mvc.perform(post("/api/cotizaciones").with(user("admin")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(form(999999L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void editorGuardaSoloSuEmpresaYElHistorialFiltra() throws Exception {
        long a = emisores.crear("Emisor pantalla A");
        long b = emisores.crear("Emisor pantalla B");
        long ca = cotizaciones.guardar(null, form(a));
        long cb = cotizaciones.guardar(null, form(b));
        String ruta = "/configuracion/empresas/" + b + "/plantilla";
        mvc.perform(get(ruta).with(user("admin"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("data-url=\"" + ruta + "\"")));
        mvc.perform(post(ruta).with(user("admin")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("empresa", datos("Emisor B modificado"), "diseno", Map.of()))))
                .andExpect(status().isOk());
        assertEquals("Emisor pantalla A", disenos.empresa(a).nombre());
        assertEquals("Emisor B modificado", disenos.empresa(b).nombre());
        mvc.perform(get("/").param("empresaId", Long.toString(a)).with(user("admin")))
                .andExpect(status().isOk()).andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<Cotizacion> lista = (List<Cotizacion>) result.getModelAndView().getModel().get("cotizaciones");
                    assertEquals(List.of(ca), lista.stream().map(Cotizacion::getId).toList());
                    assertFalse(lista.stream().anyMatch(c -> c.getId().equals(cb)));
                });
        mvc.perform(get(ruta + "/muestra.pdf").with(user("admin"))).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        mvc.perform(post(ruta).with(user("admin")).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
