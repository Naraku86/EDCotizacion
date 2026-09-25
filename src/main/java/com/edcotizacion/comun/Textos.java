package com.edcotizacion.comun;

/** Limpieza de textos capturados. */
public final class Textos {

    private Textos() {
    }

    /** Quita espacios de los extremos; vacío o solo espacios -> null (así no se imprime). */
    public static String limpio(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    public static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
