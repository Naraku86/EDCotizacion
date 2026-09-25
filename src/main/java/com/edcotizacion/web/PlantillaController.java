package com.edcotizacion.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.edcotizacion.config.ConfigService;
import com.edcotizacion.cotizacion.Cotizacion;
import com.edcotizacion.cotizacion.CotizacionService;
import com.edcotizacion.cotizacion.DatosCliente;
import com.edcotizacion.cotizacion.Partida;
import com.edcotizacion.pdf.DisenoService;
import com.edcotizacion.pdf.Empresa;
import com.edcotizacion.pdf.PdfService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.json.JsonMapper;

/** Editor de la plantilla del PDF: se edita directo sobre la hoja. */
@Controller
@RequestMapping("/configuracion/plantilla")
public class PlantillaController {

    /** Lo que edita la pantalla: el diseño y los datos de la empresa. */
    public record Datos(@NotNull Map<String, Object> diseno, @NotNull @Valid Empresa empresa) {
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
        model.addAttribute("datosJson", json.writeValueAsString(
                Map.of("diseno", disenos.diseno(), "empresa", disenos.empresa())));
        model.addAttribute("genericoJson", json.writeValueAsString(disenos.generico()));
        model.addAttribute("ejemplosJson", json.writeValueAsString(disenos.ejemplos()));
        return "plantilla";
    }

    /**
     * HTML de la hoja con una cotización de ejemplo (sin guardar). editor=true agrega los textos
     * de ayuda y botones; sin él se ve como el PDF (miniaturas de la galería).
     */
    @PostMapping(value = "/vista", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String vista(@Valid @RequestBody Datos d, @RequestParam(defaultValue = "false") boolean editor) {
        return pdf.html(muestra(), disenos.normalizar(d.diseno()), d.empresa(), editor);
    }

    @PostMapping
    @ResponseBody
    public Map<String, Boolean> guardar(@Valid @RequestBody Datos d) {
        disenos.guardar(d.diseno(), d.empresa());
        return Map.of("ok", true);
    }

    /** PDF de ejemplo con lo que hay en el editor, aunque no se haya guardado. */
    @PostMapping("/muestra.pdf")
    public ResponseEntity<byte[]> muestraSinGuardar(@RequestParam String datos) throws IOException {
        Datos d = json.readValue(datos, Datos.class);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .body(pdf.generar(muestra(), disenos.normalizar(d.diseno()), d.empresa()));
    }

    /** PDF de ejemplo con la plantilla guardada. */
    @GetMapping("/muestra.pdf")
    public ResponseEntity<byte[]> muestraGuardada() throws IOException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf.generar(muestra()));
    }

    private Cotizacion muestra() {
        Cotizacion c = cotizaciones.nueva();
        c.setFolio(config.get(ConfigService.FOLIO_PREFIJO) + "0000");
        c.setFecha(LocalDate.now());
        c.setCliente(new DatosCliente("Cliente de ejemplo S.A. de C.V.", "Juan Pérez", "55 1234 5678",
                null, null, null));
        c.reemplazarPartidas(List.of(
                partida("Producto de ejemplo con una descripción larga para ver cómo se acomoda el texto en dos renglones", "2", "1234.50"),
                partida("Servicio de instalación y configuración", "1", "850.00"),
                partida("Accesorio", "5", "99.90")));
        c.setEnvio(new BigDecimal("250"));
        CotizacionService.calcular(c);
        return c;
    }

    private static Partida partida(String descripcion, String cantidad, String precio) {
        return new Partida(descripcion, new BigDecimal(cantidad), null, new BigDecimal(precio));
    }
}
