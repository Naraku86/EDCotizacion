package com.edcotizacion.web;

import java.io.IOException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.edcotizacion.config.ConfigService;
import com.edcotizacion.cotizacion.Cotizacion;
import com.edcotizacion.cotizacion.CotizacionService;
import com.edcotizacion.cotizacion.Estado;
import com.edcotizacion.pdf.PdfService;

import tools.jackson.databind.json.JsonMapper;

@Controller
public class CotizacionController {

    private final CotizacionService service;
    private final PdfService pdf;
    private final ConfigService config;
    private final JsonMapper json;

    public CotizacionController(CotizacionService service, PdfService pdf, ConfigService config, JsonMapper json) {
        this.service = service;
        this.pdf = pdf;
        this.config = config;
        this.json = json;
    }

    @GetMapping("/")
    public String lista(@RequestParam(required = false) String q,
            @RequestParam(required = false) Estado estado, Model model) {
        model.addAttribute("cotizaciones", service.buscar(q, estado));
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        model.addAttribute("estados", Estado.values());
        return "lista";
    }

    @GetMapping("/cotizaciones/nueva")
    public String nueva(Model model) {
        return formulario(service.nueva(), model);
    }

    @GetMapping("/cotizaciones/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        return formulario(service.obtener(id), model);
    }

    private String formulario(Cotizacion c, Model model) {
        model.addAttribute("cot", c);
        model.addAttribute("cotJson", json.writeValueAsString(c));
        model.addAttribute("gananciaDefault", config.getDecimal(ConfigService.GANANCIA_DEFAULT));
        return "formulario";
    }

    @GetMapping("/cotizaciones/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable long id) throws IOException {
        Cotizacion c = service.obtener(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(PdfService.nombreArchivo(c)).build().toString())
                .body(pdf.generar(c));
    }

    @PostMapping("/cotizaciones/{id}/estado")
    public String estado(@PathVariable long id, @RequestParam Estado estado) {
        service.cambiarEstado(id, estado);
        return "redirect:/";
    }

    @PostMapping("/cotizaciones/{id}/duplicar")
    public String duplicar(@PathVariable long id) {
        return "redirect:/cotizaciones/" + service.duplicar(id) + "/editar";
    }

    @PostMapping("/cotizaciones/{id}/eliminar")
    public String eliminar(@PathVariable long id) {
        service.eliminar(id);
        return "redirect:/";
    }
}
