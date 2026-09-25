package com.edcotizacion.cotizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import com.edcotizacion.PruebaIntegracion;
import com.edcotizacion.cliente.ClienteRepository;
import com.edcotizacion.comun.NoEncontradoException;
import com.edcotizacion.cotizacion.CotizacionForm.PartidaForm;
import com.edcotizacion.producto.ProductoRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
class CotizacionServiceIT extends PruebaIntegracion {

    @Autowired
    private CotizacionService service;
    @Autowired
    private ClienteRepository clientes;
    @Autowired
    private ProductoRepository productos;
    @Autowired
    private EntityManager em;
    @Autowired
    private Environment env;

    private static BigDecimal d(String v) {
        return v == null ? null : new BigDecimal(v);
    }

    private static PartidaForm partida(String desc, String cant, String costo, String precio) {
        return new PartidaForm(desc, d(cant), d(costo), d(precio), null);
    }

    private static CotizacionForm form(String cliente, PartidaForm... partidas) {
        return new CotizacionForm(LocalDate.of(2026, 9, 25), 15,
                new DatosCliente(cliente, "Contacto", null, null, null, null),
                true, d("16"), null, "50% anticipo", null, null, null, List.of(partidas));
    }

    private long partidasEnBase(long id) {
        return em.createQuery("select count(p) from Partida p where p.cotizacion.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
    }

    @Test
    void usaLaBaseTemporalYNoLaReal() {
        assertTrue(env.getProperty("spring.datasource.url").contains(carpeta.toString()));
    }

    @Test
    void guardaConFolioTotalesYDaDeAltaCatalogos() {
        long id = service.guardar(null, form("Escuela Ñandú",
                partida("Tóner Brother TN-3610", "2", "150", "199.90"),
                partida("Cable HDMI", "1", null, "80")));

        Cotizacion c = service.obtener(id);
        assertTrue(c.getFolio().startsWith("COT-"));
        assertEquals(Estado.BORRADOR, c.getEstado());
        assertEquals(List.of("Tóner Brother TN-3610", "Cable HDMI"),
                c.getPartidas().stream().map(Partida::getDescripcion).toList());
        assertEquals(d("479.80"), c.getSubtotal());
        assertEquals(d("76.77"), c.getIva());
        assertEquals(d("556.57"), c.getTotal());
        assertEquals(d("33.27"), c.getPartidas().getFirst().getPctGanancia());

        assertTrue(clientes.findByNombre("Escuela Ñandú").isPresent());
        var toner = productos.findByDescripcion("Tóner Brother TN-3610").orElseThrow();
        assertEquals(d("150.00"), toner.getUltimoCosto());
        assertEquals(d("199.90"), toner.getUltimoPrecio());
    }

    @Test
    void editarReemplazaPartidasYConservaElFolio() {
        long id = service.guardar(null, form("Cliente Editar", partida("A", "1", null, "10"), partida("B", "1", null, "20")));
        String folio = service.obtener(id).getFolio();

        service.guardar(id, form("Cliente Editar", partida("C", "3", null, "5")));

        Cotizacion c = service.obtener(id);
        assertEquals(folio, c.getFolio());
        assertEquals(1, c.getPartidas().size());
        assertEquals(1, partidasEnBase(id), "las partidas anteriores se borran de la base");
        assertEquals(d("17.40"), c.getTotal());
    }

    @Test
    void buscaSinAcentosPorProductoYCliente() {
        long id = service.guardar(null, form("Colegio Pérez", partida("Proyector BenQ MX560", "1", null, "9000")));

        assertTrue(ids(service.buscar("benq proy", null)).contains(id));
        assertTrue(ids(service.buscar("colegio perez", null)).contains(id));
        assertFalse(ids(service.buscar("benq inexistente", null)).contains(id));
        assertTrue(ids(service.buscar(null, Estado.BORRADOR)).contains(id));
        assertFalse(ids(service.buscar(null, Estado.ACEPTADA)).contains(id));
    }

    @Test
    void duplicarCreaOtroFolioConLaFechaDeHoy() {
        long id = service.guardar(null, form("Cliente Duplicar", partida("X", "2", null, "50")));
        long copia = service.duplicar(id);

        Cotizacion a = service.obtener(id);
        Cotizacion b = service.obtener(copia);
        assertNotEquals(a.getFolio(), b.getFolio());
        assertEquals(a.getTotal(), b.getTotal());
        assertEquals(LocalDate.now(), b.getFecha());
    }

    @Test
    void cambiarEstadoYEliminar() {
        long id = service.guardar(null, form("Cliente Borrar", partida("Y", "1", null, "1")));
        service.cambiarEstado(id, Estado.ACEPTADA);
        assertEquals(Estado.ACEPTADA, service.obtener(id).getEstado());

        service.eliminar(id);
        assertThrows(NoEncontradoException.class, () -> service.obtener(id));
        assertEquals(0, partidasEnBase(id));
    }

    @Test
    void inexistenteLanzaNoEncontrado() {
        assertThrows(NoEncontradoException.class, () -> service.obtener(999_999));
        assertThrows(NoEncontradoException.class, () -> service.eliminar(999_999));
    }

    private static List<Long> ids(List<Cotizacion> lista) {
        return lista.stream().map(Cotizacion::getId).toList();
    }
}
