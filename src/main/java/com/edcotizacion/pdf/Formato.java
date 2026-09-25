package com.edcotizacion.pdf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.edcotizacion.cotizacion.Montos;

/** Funciones de formato disponibles como ${f.*} en las plantillas. */
@Component("f")
public class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String moneda(BigDecimal v) {
        return Montos.moneda(v);
    }

    public String cantidad(BigDecimal v) {
        return Montos.cantidad(v);
    }

    public String pct(BigDecimal v) {
        return Montos.pct(v);
    }

    public String fecha(LocalDate d) {
        return d == null ? "" : d.format(FECHA);
    }
}
