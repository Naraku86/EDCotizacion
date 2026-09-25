package com.edcotizacion.pdf;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diseño del PDF: qué plantilla (acomodo) se usa, colores, textos editables y secciones
 * opcionales. Se guarda como JSON en config ('plantilla.diseno'); DisenoService lo completa
 * con los valores del diseño genérico para que nunca falte una clave.
 *
 * @param textos títulos, encabezados de columnas y pie: lo que se edita haciendo clic en la hoja
 * @param firma  titulo = nombre de quien firma (vacío = el ejecutivo), texto = puesto
 */
public record Diseno(
        String plantilla,
        String papel,
        Map<String, String> colores,
        Map<String, String> textos,
        boolean mostrarNombre,
        boolean mayusculas,
        boolean numeroPartida,
        boolean paginas,
        Seccion banco,
        Seccion nota,
        Seccion firma) {

    public static final List<String> PLANTILLAS = List.of("clasica", "minimalista", "moderna", "compacta");

    /** Sección opcional que se agrega con "+ ..." en el editor. */
    public record Seccion(boolean activo, String titulo, String texto) {

        static Seccion oVacia(Seccion s) {
            return s == null ? new Seccion(false, null, null) : s;
        }
    }

    public Diseno {
        plantilla = PLANTILLAS.contains(plantilla) ? plantilla : "clasica";
        papel = "a4".equals(papel) ? "a4" : "carta";
        colores = copia(colores);
        textos = copia(textos);
        banco = Seccion.oVacia(banco);
        nota = Seccion.oVacia(nota);
        firma = Seccion.oVacia(firma);
    }

    /** Texto por clave; "" si no existe. */
    public String t(String clave) {
        return textos.getOrDefault(clave, "");
    }

    private static Map<String, String> copia(Map<String, String> m) {
        return m == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(m));
    }
}
