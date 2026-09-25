package com.edcotizacion;

import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base de las pruebas con la app completa: cada clase usa una base SQLite nueva en una
 * carpeta temporal (nunca la de ~/EDCotizacion) y no abre el navegador.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class PruebaIntegracion {

    @TempDir
    protected static Path carpeta;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry r) {
        r.add("app.home", () -> carpeta.toString());
        r.add("app.abrir-navegador", () -> "false");
    }
}
