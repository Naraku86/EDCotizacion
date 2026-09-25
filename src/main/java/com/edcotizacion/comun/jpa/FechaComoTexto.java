package com.edcotizacion.comun.jpa;

import java.time.LocalDate;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Fechas como TEXT ISO ("2026-09-25"), legibles y ordenables en SQLite. */
@Converter(autoApply = true)
public class FechaComoTexto implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate v) {
        return v == null ? null : v.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String s) {
        return s == null || s.isBlank() ? null : LocalDate.parse(s);
    }
}
