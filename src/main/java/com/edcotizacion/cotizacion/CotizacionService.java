package com.edcotizacion.cotizacion;

import static com.edcotizacion.cotizacion.Montos.nz;
import static com.edcotizacion.cotizacion.Montos.r2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edcotizacion.cliente.Cliente;
import com.edcotizacion.cliente.ClienteRepository;
import com.edcotizacion.config.ConfigService;
import com.edcotizacion.producto.ProductoRepository;

@Service
public class CotizacionService {

    private final CotizacionRepository cotizaciones;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final ConfigService config;

    public CotizacionService(CotizacionRepository cotizaciones, ClienteRepository clientes,
            ProductoRepository productos, ConfigService config) {
        this.cotizaciones = cotizaciones;
        this.clientes = clientes;
        this.productos = productos;
        this.config = config;
    }

    /** Cotización en blanco con los valores por defecto de Configuración. */
    public Cotizacion nueva() {
        Cotizacion c = new Cotizacion();
        c.setFecha(LocalDate.now());
        c.setVigenciaDias(config.getInt(ConfigService.VIGENCIA_DEFAULT));
        c.setTasaIva(config.getDecimal(ConfigService.IVA_TASA));
        c.setFormaPago(config.get(ConfigService.FORMA_PAGO));
        c.setTiempoEntrega(config.get(ConfigService.TIEMPO_ENTREGA));
        c.setGarantia(config.get(ConfigService.GARANTIA));
        c.setObservaciones(config.get(ConfigService.OBSERVACIONES));
        return c;
    }

    public Cotizacion obtener(long id) {
        return cotizaciones.porId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cotización " + id));
    }

    public List<Cotizacion> buscar(String texto, Estado estado) {
        return cotizaciones.buscar(texto, estado);
    }

    /**
     * Guarda la cotización: da de alta cliente y productos nuevos, recalcula
     * importes y totales, y asigna folio si es nueva. Devuelve el id.
     */
    @Transactional
    public long guardar(Cotizacion c) {
        validar(c);
        limpiar(c.getCliente());
        // Las condiciones vacías no se imprimen en el PDF
        c.setFormaPago(nulo(c.getFormaPago()));
        c.setTiempoEntrega(nulo(c.getTiempoEntrega()));
        c.setGarantia(nulo(c.getGarantia()));
        c.setObservaciones(nulo(c.getObservaciones()));
        c.getCliente().setId(clientes.guardar(c.getCliente()));

        calcular(c);
        for (Partida p : c.getPartidas()) {
            p.setProductoId(productos.guardar(p.getDescripcion(), p.getCosto(), p.getPrecioUnitario()));
        }

        c.setModificada(LocalDateTime.now());
        if (c.getId() == null) {
            c.setFolio(config.tomarFolio());
            c.setEstado(Estado.BORRADOR);
            c.setCreada(c.getModificada());
            return cotizaciones.insertar(c);
        }
        cotizaciones.actualizar(c);
        return c.getId();
    }

    /** Recalcula importes y totales. El envío no lleva IVA. */
    public static void calcular(Cotizacion c) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Partida p : c.getPartidas()) {
            p.setCosto(r2(p.getCosto()));
            p.setPrecioUnitario(r2(p.getPrecioUnitario()));
            p.setImporte(r2(p.getCantidad().multiply(p.getPrecioUnitario())));
            p.setPctGanancia(Montos.pctGanancia(p.getCosto(), p.getPrecioUnitario()));
            subtotal = subtotal.add(p.getImporte());
        }
        c.setEnvio(r2(nz(c.getEnvio())));
        c.setSubtotal(r2(subtotal));
        c.setIva(c.isAplicaIva() ? Montos.porcentaje(subtotal, c.getTasaIva()) : r2(BigDecimal.ZERO));
        c.setTotal(c.getSubtotal().add(c.getIva()).add(c.getEnvio()));
    }

    @Transactional
    public long duplicar(long id) {
        Cotizacion c = obtener(id);
        c.setId(null);
        c.setFecha(LocalDate.now());
        return guardar(c);
    }

    public void cambiarEstado(long id, Estado estado) {
        cotizaciones.cambiarEstado(id, estado);
    }

    public void eliminar(long id) {
        cotizaciones.eliminar(id);
    }

    private static void validar(Cotizacion c) {
        List<String> errores = new ArrayList<>();
        if (c.getCliente() == null || blank(c.getCliente().getNombre())) {
            errores.add("Captura el nombre del cliente.");
        }
        c.getPartidas().removeIf(p -> blank(p.getDescripcion()) && p.getPrecioUnitario() == null);
        if (c.getPartidas().isEmpty()) {
            errores.add("Agrega al menos un producto.");
        }
        for (int i = 0; i < c.getPartidas().size(); i++) {
            Partida p = c.getPartidas().get(i);
            String n = "Partida " + (i + 1) + ": ";
            if (blank(p.getDescripcion())) errores.add(n + "falta la descripción.");
            if (p.getCantidad() == null || p.getCantidad().signum() <= 0) errores.add(n + "la cantidad debe ser mayor a 0.");
            if (p.getPrecioUnitario() == null) errores.add(n + "falta el precio.");
            if (p.getDescripcion() != null) p.setDescripcion(p.getDescripcion().trim());
        }
        if (c.getFecha() == null) errores.add("Falta la fecha.");
        if (c.getTasaIva() == null) errores.add("Falta la tasa de IVA.");
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
    }

    private static void limpiar(Cliente cl) {
        cl.setNombre(cl.getNombre().trim());
        cl.setContacto(nulo(cl.getContacto()));
        cl.setTelefono(nulo(cl.getTelefono()));
        cl.setEmail(nulo(cl.getEmail()));
        cl.setRfc(nulo(cl.getRfc()));
        cl.setDireccion(nulo(cl.getDireccion()));
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String nulo(String s) {
        return blank(s) ? null : s.trim();
    }
}
