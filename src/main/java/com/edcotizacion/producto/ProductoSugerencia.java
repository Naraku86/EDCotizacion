package com.edcotizacion.producto;

import java.math.BigDecimal;

/** Producto para el autocompletado: descripción y últimos costo/precio usados. */
public record ProductoSugerencia(String descripcion, BigDecimal ultimoCosto, BigDecimal ultimoPrecio) {

    public static ProductoSugerencia de(Producto p) {
        return new ProductoSugerencia(p.getDescripcion(), p.getUltimoCosto(), p.getUltimoPrecio());
    }
}
