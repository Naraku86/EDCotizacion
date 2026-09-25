package com.edcotizacion.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.edcotizacion.cotizacion.Cotizacion;
import com.edcotizacion.demo.DemoForm;
import com.edcotizacion.demo.LimiteDemo;
import com.edcotizacion.demo.LimiteDemo.LimiteExcedidoException;
import com.edcotizacion.pdf.DisenoService;
import com.edcotizacion.pdf.PdfService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import tools.jackson.databind.json.JsonMapper;

/**
 * Demo público (sin sesión): se capturan empresa, diseño y cotización en una sola página y se
 * descarga el PDF. No usa ningún repositorio: nada de lo capturado llega a la base de datos.
 * Se apaga con app.demo.activo=false.
 */
@Controller
@RequestMapping("/demo")
@ConditionalOnProperty(name = "app.demo.activo", havingValue = "true", matchIfMissing = true)
public class DemoController {

    private final DisenoService disenos;
    private final PdfService pdf;
    private final LimiteDemo limite;
    private final JsonMapper json;

    public DemoController(DisenoService disenos, PdfService pdf, LimiteDemo limite, JsonMapper json) {
        this.disenos = disenos;
        this.pdf = pdf;
        this.limite = limite;
        this.json = json;
    }

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("ejemplosJson", json.writeValueAsString(disenos.ejemplos()));
        model.addAttribute("maxPartidas", DemoForm.MAX_PARTIDAS);
        return "demo";
    }

    /** HTML de la hoja para la vista previa (el mismo que se convierte a PDF). */
    @PostMapping(value = "/vista", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String vista(@Valid @RequestBody DemoForm f, HttpServletRequest request) {
        limite.vista(request.getRemoteAddr());
        return pdf.html(f.cotizacion(), disenos.normalizar(f.disenoCrudo()), f.empresa(), false);
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@Valid @RequestBody DemoForm f, HttpServletRequest request) throws Exception {
        limite.pdf(request.getRemoteAddr());
        Cotizacion c = f.cotizacion();
        byte[] archivo = limite.generar(() -> pdf.generar(c, disenos.normalizar(f.disenoCrudo()), f.empresa()));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(PdfService.nombreArchivo(c)).build().toString())
                .body(archivo);
    }

    @ExceptionHandler(LimiteExcedidoException.class)
    @ResponseBody
    public ResponseEntity<ErroresApi.Error> limiteExcedido(LimiteExcedidoException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header(HttpHeaders.RETRY_AFTER, "60")
                .body(new ErroresApi.Error(e.getMessage()));
    }
}
