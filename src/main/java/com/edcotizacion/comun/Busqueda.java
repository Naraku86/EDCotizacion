package com.edcotizacion.comun;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Búsqueda sin acentos ni mayúsculas: "toner bro" encuentra "Tóner Brother TN-3610".
 * SQLite no lo hace por sí solo, así que se filtra en Java (los catálogos son chicos).
 */
public final class Busqueda {

    private final String[] palabras;

    public Busqueda(String texto) {
        this.palabras = normalizar(texto).split("\\s+");
    }

    /** true si el texto contiene todas las palabras buscadas, en cualquier orden. */
    public boolean coincide(String texto) {
        String t = normalizar(texto);
        for (String p : palabras) {
            if (!t.contains(p)) {
                return false;
            }
        }
        return true;
    }

    public static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
