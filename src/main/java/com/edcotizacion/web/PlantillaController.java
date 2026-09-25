package com.edcotizacion.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.edcotizacion.config.ConfigService;
import com.edcotizacion.cotizacion.Cotizacion;
import com.edcotizacion.cotizacion.CotizacionService;
import com.edcotizacion.cotizacion.Partida;
import com.edcotizacion.pdf.DisenoService;
import com.edcotizacion.pdf.Empresa;
import com.edcotizacion.pdf.PdfService;

import tools.jackson.databind.json.JsonMapper;

/** Editor visual de la plantilla del PDF (arrastrar y soltar bloques). */
@Controller
@RequestMapping("/configuracion/plantilla")
public class PlantillaController {

    /** Lo que edita la pantalla: el diseño y los datos de la empresa. */
    public record Datos(Map<String, Object> diseno, Empresa empresa) {
    }

    private final DisenoService disenos;
    private final PdfService pdf;
    private final CotizacionService cotizaciones;
    private final ConfigService config;
    private final JsonMapper json;

    public PlantillaController(DisenoService disenos, PdfService pdf, CotizacionService cotizaciones,
            ConfigService config, JsonMapper json) {
        this.disenos = disenos;
        this.pdf = pdf;
        this.cotizaciones = cotizaciones;
        this.config = config;
        this.json = json;
    }

    @GetMapping
    public String editor(Model model) {
        model.addAttribute("datosJson", json.writeValueAsString(new Datos(disenos.diseno(), disenos.empresa())));
        model.addAttribute("genericoJson", json.writeValueAsString(disenos.generico()));
        return "plantilla";
    }

    /** HTML de la hoja con una cotización de ejemplo, para el lienzo del editor (sin guardar). */
    @PostMapping(value = "/vista", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String vista(@RequestBody Datos d) {
        return pdf.html(muestra(), d.diseno(), d.empresa());
    }

    @PostMapping
    @ResponseBody
    public Map<String, Boolean> guardar(@RequestBody Datos d) {
        disenos.guardar(d.diseno(), d.empresa());
        return Map.of("ok", true);
    }

    /** PDF de ejemplo con lo que hay en el editor, aunque no se haya guardado. */
    @PostMapping("/muestra.pdf")
    public ResponseEntity<byte[]> muestraSinGuardar(@RequestParam String datos) throws IOException {
        Datos d = json.readValue(datos, Datos.class);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .body(pdf.generar(muestra(), d.diseno(), d.empresa()));
    }

    /** PDF de ejemplo con la plantilla guardada. */
    @GetMapping("/muestra.pdf")
    public ResponseEntity<byte[]> muestraGuardada() throws IOException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf.generar(muestra()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> error(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    private Cotizacion muestra() {
        Cotizacion c = cotizaciones.nueva();
        c.setFolio(config.get(ConfigService.FOLIO_PREFIJO) + "0000");
        c.setFecha(LocalDate.now());
        c.getCliente().setNombre("Cliente de ejemplo S.A. de C.V.");
        c.getCliente().setContacto("Juan Pérez");
        c.getCliente().setTelefono("55 1234 5678");
        c.getPartidas().add(partida("Producto de ejemplo con una descripción larga para ver cómo se acomoda el texto en dos renglones", "2", "1234.50"));
        c.getPartidas().add(partida("Servicio de instalación y configuración", "1", "850.00"));
        c.getPartidas().add(partida("Accesorio", "5", "99.90"));
        c.setEnvio(new BigDecimal("250"));
        CotizacionService.calcular(c);
        return c;
    }

    private static Partida partida(String descripcion, String cantidad, String precio) {
        Partida p = new Partida();
        p.setDescripcion(descripcion);
        p.setCantidad(new BigDecimal(cantidad));
        p.setPrecioUnitario(new BigDecimal(precio));
        return p;
    }
}
