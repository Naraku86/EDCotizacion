package com.edcotizacion.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;

import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.edcotizacion.cotizacion.Cotizacion;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/** Arma el HTML del PDF a partir del diseño (templates/pdf/cotizacion.html) y lo convierte a PDF. */
@Service
public class PdfService {

    private final DisenoService disenos;
    private final Formato formato;
    private final ITemplateEngine engine;

    public PdfService(DisenoService disenos, Formato formato, ITemplateEngine engine) {
        this.disenos = disenos;
        this.formato = formato;
        this.engine = engine;
    }

    public byte[] generar(Cotizacion c) throws IOException {
        return generar(c, disenos.diseno(), disenos.empresa());
    }

    public byte[] generar(Cotizacion c, Diseno diseno, Empresa empresa) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            fuente(builder, "Exo2-Bold.ttf", "Exo2", 700);
            fuente(builder, "LiberationSans-Regular.ttf", "Liberation", 400);
            fuente(builder, "LiberationSans-Bold.ttf", "Liberation", 700);
            builder.withHtmlContent(html(c, diseno, empresa, false), null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        }
    }

    /**
     * El mismo HTML sirve para el PDF y para el editor. Con editor = true los textos vacíos
     * se muestran con su ayuda ("Tu RFC") y aparecen los botones para subir logo y agregar secciones.
     */
    public String html(Cotizacion c, Diseno diseno, Empresa empresa, boolean editor) {
        Context ctx = new Context();
        ctx.setVariable("c", c);
        ctx.setVariable("f", formato);
        ctx.setVariable("d", diseno);
        ctx.setVariable("e", empresa.limpia());
        ctx.setVariable("s", new Estilo(diseno.getColores()));
        ctx.setVariable("ed", editor);
        return engine.process("pdf/cotizacion", ctx);
    }

    private void fuente(PdfRendererBuilder builder, String archivo, String familia, int peso) {
        builder.useFont(() -> getClass().getResourceAsStream("/static/fuentes/" + archivo),
                familia, peso, FontStyle.NORMAL, true);
    }

    /** COT-0001_Escuela_Primaria_Lic.pdf */
    public static String nombreArchivo(Cotizacion c) {
        String cliente = Normalizer.normalize(c.getCliente().getNombre(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        if (cliente.length() > 40) {
            cliente = cliente.substring(0, 40);
        }
        return c.getFolio() + "_" + cliente + ".pdf";
    }
}
