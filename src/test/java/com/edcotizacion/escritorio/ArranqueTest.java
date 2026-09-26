package com.edcotizacion.escritorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ArranqueTest {

    @Test
    void losArgumentosMandan() {
        Arranque a = Arranque.de(new String[] { "--app.modo=servidor", "--server.port=9123", "--app.home=/tmp/datos" });
        assertFalse(a.escritorio());
        assertEquals(9123, a.puerto());
        assertEquals(Path.of("/tmp/datos"), a.home());
        assertEquals("http://localhost:9123", a.url());
    }

    @Test
    void sinArgumentosEsEscritorioEnElPuerto8090() {
        Arranque a = Arranque.de(new String[0]);
        // en la máquina de pruebas no se definen app.modo ni server.port
        assertTrue(a.escritorio());
        assertEquals(8090, a.puerto());
    }

    @Test
    void rechazaUnModoDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> Arranque.de(new String[] { "--app.modo=otro" }));
    }
}
