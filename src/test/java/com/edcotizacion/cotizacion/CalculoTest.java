package com.edcotizacion.cotizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CalculoTest {

    private static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    private static Partida partida(String cantidad, String costo, String precio) {
        Partida p = new Partida();
        p.setDescripcion("x");
        p.setCantidad(d(cantidad));
        p.setCosto(costo == null ? null : d(costo));
        p.setPrecioUnitario(d(precio));
        return p;
    }

    @Test
    void precioSugeridoEsCostoMasPorcentaje() {
        assertEquals(d("9200.10"), Montos.precioSugerido(d("7077"), d("30")));
        assertEquals(d("130.00"), Montos.precioSugerido(d("100"), d("30")));
    }

    @Test
    void precioConIvaIncluidoDaExactoElMontoFinal() {
        BigDecimal precio = Montos.precioSinIva(d("1000"), d("16"));
        assertEquals(d("862.07"), precio);

        Cotizacion c = new Cotizacion();
        c.setTasaIva(d("16"));
        c.getPartidas().add(partida("1", null, precio.toPlainString()));
        CotizacionService.calcular(c);
        assertEquals(d("1000.00"), c.getTotal());
    }

    @Test
    void envioNoLlevaIva() {
        Cotizacion c = new Cotizacion();
        c.setTasaIva(d("16"));
        c.setEnvio(d("150"));
        c.getPartidas().add(partida("1", "7077", "9200"));
        c.getPartidas().add(partida("1", "100", "130"));
        c.getPartidas().add(partida("1", null, "862.07"));
        CotizacionService.calcular(c);

        assertEquals(d("10192.07"), c.getSubtotal());
        assertEquals(d("1630.73"), c.getIva());
        assertEquals(d("11972.80"), c.getTotal());
    }

    @Test
    void sinIva() {
        Cotizacion c = new Cotizacion();
        c.setTasaIva(d("16"));
        c.setAplicaIva(false);
        c.getPartidas().add(partida("2", "100", "130"));
        CotizacionService.calcular(c);

        assertEquals(d("0.00"), c.getIva());
        assertEquals(d("260.00"), c.getTotal());
    }

    @Test
    void totalSiempreConDosDecimales() {
        // En el PDF original salía $10,669.682
        Cotizacion c = new Cotizacion();
        c.setTasaIva(d("16"));
        c.getPartidas().add(partida("1", null, "9198"));
        CotizacionService.calcular(c);

        assertEquals(d("1471.68"), c.getIva());
        assertEquals(d("10669.68"), c.getTotal());
        assertEquals("$10,669.68", Montos.moneda(c.getTotal()));
    }

    @Test
    void porcentajeDeGananciaReal() {
        Cotizacion c = new Cotizacion();
        c.setTasaIva(d("16"));
        Partida p = partida("3", "1450", "1885");
        c.getPartidas().add(p);
        CotizacionService.calcular(c);

        assertEquals(d("30.00"), p.getPctGanancia());
        assertEquals(d("1305.00"), p.getGanancia());
    }
}
