package com.edcotizacion.cotizacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Redondeo, conversión y formato de montos. Todo dinero va a 2 decimales. */
public final class Montos {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private Montos() {
    }

    public static BigDecimal r2(BigDecimal v) {
        return v == null ? null : v.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Precio = costo + % de ganancia. */
    public static BigDecimal precioSugerido(BigDecimal costo, BigDecimal pctGanancia) {
        return r2(costo.multiply(BigDecimal.ONE.add(pctGanancia.divide(CIEN))));
    }

    /** Precio antes de IVA para que precio + IVA dé exactamente el monto indicado. */
    public static BigDecimal precioSinIva(BigDecimal precioConIva, BigDecimal tasaIva) {
        return precioConIva.divide(BigDecimal.ONE.add(tasaIva.divide(CIEN)), 2, RoundingMode.HALF_UP);
    }

    /** % de ganancia real dado un costo y un precio (null si no hay costo). */
    public static BigDecimal pctGanancia(BigDecimal costo, BigDecimal precio) {
        if (costo == null || costo.signum() == 0 || precio == null) {
            return null;
        }
        return precio.subtract(costo).multiply(CIEN).divide(costo, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal porcentaje(BigDecimal base, BigDecimal tasa) {
        return r2(base.multiply(tasa).divide(CIEN));
    }

    // --- conversión para SQLite (TEXT) ---

    public static BigDecimal decimal(String s) {
        return s == null || s.isBlank() ? null : new BigDecimal(s);
    }

    public static String texto(BigDecimal v) {
        return v == null ? null : v.toPlainString();
    }

    // --- formato para pantalla y PDF ---

    private static DecimalFormat formato(String patron) {
        return new DecimalFormat(patron, DecimalFormatSymbols.getInstance(Locale.US));
    }

    /** $9,198.00 */
    public static String moneda(BigDecimal v) {
        return formato("$#,##0.00").format(r2(nz(v)));
    }

    /** 1, 2.5 (sin ceros de más) */
    public static String cantidad(BigDecimal v) {
        return formato("#,##0.###").format(nz(v));
    }

    /** 16, 10.5 */
    public static String pct(BigDecimal v) {
        return formato("0.##").format(nz(v));
    }
}
