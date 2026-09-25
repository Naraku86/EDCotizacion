package com.edcotizacion.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.edcotizacion.empresa.EmisorService;

import tools.jackson.databind.json.JsonMapper;

class DisenoServiceTest {

    private final DisenoService service = new DisenoService(mock(EmisorService.class), JsonMapper.builder().build());

    @Test
    void completaConElGenericoYValida() {
        Diseno d = service.normalizar(Map.of("plantilla", "inventada", "papel", "a4",
                "textos", Map.of("titulo", "PRESUPUESTO")));
        assertEquals("clasica", d.plantilla());
        assertEquals("a4", d.papel());
        assertEquals("PRESUPUESTO", d.t("titulo"));
        assertEquals("COTIZADO A", d.t("cliente"), "lo que falta sale del genérico");
        assertFalse(d.banco().activo());
    }

    @Test
    void convierteElFormatoAnteriorPorBloques() {
        Map<String, Object> viejo = Map.of(
                "estilo", Map.of("primario", "#184E62", "acento", "#1A9C8D"),
                "pie", Map.of("texto", "EV • Cómputo", "paginas", false),
                "bloques", List.of(
                        Map.of("tipo", "encabezado", "titulo", "COTIZACIÓN", "mayusculas", false),
                        Map.of("tipo", "partes", "tituloProveedor", "DE", "tituloCliente", "PARA"),
                        Map.of("tipo", "firma", "nombre", "Edgar", "puesto", "Ventas")));
        Diseno d = service.normalizar(viejo);
        assertEquals("#184E62", d.colores().get("primario"));
        assertEquals("EV • Cómputo", d.t("pie"));
        assertFalse(d.paginas());
        assertFalse(d.mayusculas());
        assertEquals("DE", d.t("proveedor"));
        assertTrue(d.firma().activo());
        assertEquals("Ventas", d.firma().texto());
    }

    @Test
    void recortaTextosEnormes() {
        Diseno d = service.normalizar(Map.of("textos", Map.of("pie", "x".repeat(10_000))));
        assertEquals(Diseno.MAX_LARGO, d.t("pie").length());
    }
}
