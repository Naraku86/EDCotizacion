package com.edcotizacion.comun.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Fecha y hora como TEXT ISO ("2026-09-25T10:30:00"). */
@Converter(autoApply = true)
public class FechaHoraComoTexto implements AttributeConverter<LocalDateTime, String> {

    @Override
    public String convertToDatabaseColumn(LocalDateTime v) {
        return v == null ? null : v.toString();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String s) {
        return s == null || s.isBlank() ? null : LocalDateTime.parse(s);
    }
}
