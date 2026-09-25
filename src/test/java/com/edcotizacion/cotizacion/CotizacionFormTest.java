package com.edcotizacion.cotizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.edcotizacion.cotizacion.CotizacionForm.PartidaForm;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CotizacionFormTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void iniciar() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void cerrar() {
        fabrica.close();
    }

    private static CotizacionForm form(DatosCliente cliente, PartidaForm... partidas) {
        return new CotizacionForm(LocalDate.of(2026, 9, 25), 15, cliente, true, new BigDecimal("16"), null,
                "  ", "3 días ", null, null, Arrays.asList(partidas));
    }

    private static PartidaForm partida(String desc, String cant, String precio) {
        return new PartidaForm(desc, cant == null ? null : new BigDecimal(cant), null,
                precio == null ? null : new BigDecimal(precio), null);
    }

    private static Set<String> errores(Object o) {
        return validador.validate(o).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    @Test
    void renglonesEnBlancoSeIgnoranYLosTextosSeLimpian() {
        CotizacionForm f = form(new DatosCliente("  Escuela  ", "", null, null, null, null),
                partida("Laptop", "1", "100"), partida("  ", "1", null));
        assertEquals(1, f.partidas().size());
        assertEquals("Escuela", f.cliente().nombre());
        assertNull(f.cliente().contacto());
        assertNull(f.formaPago());
        assertEquals("3 días", f.tiempoEntrega());
        assertEquals(BigDecimal.ZERO, f.envio());
        assertTrue(errores(f).isEmpty());
    }

    @Test
    void validaClienteYPartidas() {
        CotizacionForm f = form(new DatosCliente(" ", null, null, "no-es-correo", null, null),
                partida("Laptop", "0", "100"), partida(null, "1", "-5"));
        Set<String> e = errores(f);
        assertTrue(e.contains("Captura el nombre del cliente."), e::toString);
        assertTrue(e.contains("El correo del cliente no es válido."), e::toString);
        assertTrue(e.contains("la cantidad debe ser mayor a 0."), e::toString);
        assertTrue(e.contains("falta la descripción."), e::toString);
        assertTrue(e.contains("el precio no puede ser negativo."), e::toString);
    }

    @Test
    void sinPartidasNoSePuedeGuardar() {
        CotizacionForm f = form(new DatosCliente("Escuela", null, null, null, null, null), partida("", null, null));
        assertEquals(List.of(), f.partidas());
        assertTrue(errores(f).contains("Agrega al menos un producto."));
    }
}
