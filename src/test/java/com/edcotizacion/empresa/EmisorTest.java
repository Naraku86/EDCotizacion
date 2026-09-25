package com.edcotizacion.empresa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EmisorTest {

    @Test
    void recortaElNombreYRechazaVaciosOLargos() {
        Emisor e = new Emisor("  Mi Empresa  ", "{}", "{}");
        assertEquals("Mi Empresa", e.getNombre());
        assertThrows(IllegalArgumentException.class, () -> new Emisor(null, "{}", "{}"));
        assertThrows(IllegalArgumentException.class, () -> e.actualizar(" ", "{}", "{}"));
        assertThrows(IllegalArgumentException.class,
                () -> e.actualizar("x".repeat(Emisor.NOMBRE_MAXIMO + 1), "{}", "{}"));
        assertEquals("Mi Empresa", e.getNombre(), "un cambio rechazado no toca la entidad");
    }

    @Test
    void exigeDatosYDiseno() {
        assertThrows(IllegalArgumentException.class, () -> new Emisor("Mi Empresa", null, "{}"));
        assertThrows(IllegalArgumentException.class, () -> new Emisor("Mi Empresa", "{}", " "));
    }
}
