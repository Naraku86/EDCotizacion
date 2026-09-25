package com.edcotizacion.cotizacion;

public enum Estado {
    BORRADOR("Borrador"),
    ENVIADA("Enviada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Solo las cotizaciones abiertas pueden vencer. */
    public boolean isAbierta() {
        return this == BORRADOR || this == ENVIADA;
    }
}
