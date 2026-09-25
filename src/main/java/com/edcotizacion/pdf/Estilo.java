package com.edcotizacion.pdf;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Colores y medidas del diseño, disponibles como ${s.*} en la plantilla del PDF.
 * Todo lo que viene del editor se valida aquí antes de llegar al CSS.
 * Solo se eligen dos colores; los tonos claros se sacan mezclando con blanco.
 */
public class Estilo {

    private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{6}");

    private final String primario;
    private final String acento;
    private final String texto;
    private final String fuenteMarca;
    private final double tamano;

    public Estilo(Object estilo) {
        Map<?, ?> m = estilo instanceof Map<?, ?> mapa ? mapa : Map.of();
        primario = hex(m.get("primario"), "#243B53");
        acento = hex(m.get("acento"), "#3E7CB1");
        texto = hex(m.get("texto"), "#2B3A40");
        fuenteMarca = "Liberation".equals(m.get("fuenteMarca")) ? "Liberation" : "Exo2";
        tamano = rango(m.get("tamano"), 8.5, 7, 12);
    }

    public String getPrimario() { return primario; }
    public String getAcento() { return acento; }
    public String getTexto() { return texto; }
    public String getFuenteMarca() { return fuenteMarca; }
    public String getTamano() { return fmt(tamano); }

    /** Fondo de proveedor/cliente. */
    public String getFondo() { return mezcla(acento, 0.94); }
    /** Fondo de totales. */
    public String getFondo2() { return mezcla(acento, 0.91); }
    /** Fondo de las etiquetas de condiciones. */
    public String getFondo3() { return mezcla(acento, 0.88); }
    public String getBorde() { return mezcla(acento, 0.78); }
    public String getRaya() { return mezcla(acento, 0.84); }
    /** Texto claro sobre el color primario (lema). */
    public String getSobrePrimario() { return mezcla(primario, 0.82); }
    public String getTenue() { return "#7A8A90"; }

    /** Color por nombre: primario, acento, gris o texto. */
    public String color(Object nombre) {
        return switch (String.valueOf(nombre)) {
            case "primario" -> primario;
            case "acento" -> acento;
            case "gris" -> getTenue();
            default -> texto;
        };
    }

    public String alinear(Object a) {
        return switch (String.valueOf(a)) {
            case "centro" -> "center";
            case "derecha" -> "right";
            case "justificado" -> "justify";
            default -> "left";
        };
    }

    public String num(Object v, double def, double min, double max) {
        return fmt(rango(v, def, min, max));
    }

    /** Solo acepta imágenes incrustadas (data:image/...); cualquier otra cosa se ignora. */
    public String imagen(Object src) {
        return src instanceof String s && s.startsWith("data:image/") && !s.contains("\"") ? s : null;
    }

    private static String hex(Object v, String def) {
        return v instanceof String s && HEX.matcher(s).matches() ? s.toUpperCase() : def;
    }

    private static double rango(Object v, double def, double min, double max) {
        double d;
        if (v instanceof Number n) {
            d = n.doubleValue();
        } else {
            try {
                d = Double.parseDouble(String.valueOf(v));
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return Double.isFinite(d) ? Math.max(min, Math.min(max, d)) : def;
    }

    private static String fmt(double d) {
        return String.format(Locale.ROOT, "%.1f", d);
    }

    /** Mezcla el color con blanco: t = 0 deja el color, t = 1 es blanco. */
    static String mezcla(String hex, double t) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        int r = canal(rgb >> 16, t), g = canal(rgb >> 8, t), b = canal(rgb, t);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private static int canal(int c, double t) {
        c &= 0xFF;
        return (int) Math.round(c + (255 - c) * t);
    }
}
