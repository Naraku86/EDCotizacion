package com.edcotizacion.comun.jpa;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Los montos se guardan como TEXT ("1234.50"): SQLite no tiene decimal exacto y como REAL
 * se perderían centavos. Se aplica a todos los BigDecimal de las entidades.
 */
@Converter(autoApply = true)
public class DecimalComoTexto implements AttributeConverter<BigDecimal, String> {

    @Override
    public String convertToDatabaseColumn(BigDecimal v) {
        return v == null ? null : v.toPlainString();
    }

    @Override
    public BigDecimal convertToEntityAttribute(String s) {
        return s == null || s.isBlank() ? null : new BigDecimal(s);
    }
}
