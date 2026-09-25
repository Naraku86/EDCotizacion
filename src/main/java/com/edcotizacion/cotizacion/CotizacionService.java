package com.edcotizacion.cotizacion;

import static com.edcotizacion.cotizacion.Montos.nz;
import static com.edcotizacion.cotizacion.Montos.r2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edcotizacion.cliente.ClienteRepository;
import com.edcotizacion.comun.NoEncontradoException;
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
                .orElseThrow(() -> new NoEncontradoException("No existe la cotización " + id));
    }

    public List<Cotizacion> buscar(String texto, Estado estado) {
        return cotizaciones.buscar(texto, estado);
    }

    /**
     * Guarda lo capturado (ya validado): da de alta cliente y productos nuevos, recalcula
     * importes y totales, y asigna folio si es nueva. id null = nueva. Devuelve el id.
     */
    @Transactional
    public long guardar(Long id, CotizacionForm f) {
        Cotizacion c = id == null ? new Cotizacion() : obtener(id);
        c.setFecha(f.fecha());
        c.setVigenciaDias(f.vigenciaDias());
        c.setCliente(f.cliente());
        c.setClienteId(clientes.guardar(f.cliente()));
        c.setAplicaIva(f.aplicaIva());
        c.setTasaIva(f.tasaIva());
        c.setEnvio(f.envio());
        c.setFormaPago(f.formaPago());
        c.setTiempoEntrega(f.tiempoEntrega());
        c.setGarantia(f.garantia());
        c.setObservaciones(f.observaciones());
        c.setPartidas(f.partidas().stream().map(CotizacionService::partida).collect(Collectors.toCollection(ArrayList::new)));

        calcular(c);
        for (Partida p : c.getPartidas()) {
            p.setProductoId(productos.guardar(p.getDescripcion(), p.getCosto(), p.getPrecioUnitario()));
        }

        c.setModificada(LocalDateTime.now());
        if (id == null) {
            c.setFolio(config.tomarFolio());
            c.setEstado(Estado.BORRADOR);
            c.setCreada(c.getModificada());
            return cotizaciones.insertar(c);
        }
        cotizaciones.actualizar(c);
        return id;
    }

    private static Partida partida(CotizacionForm.PartidaForm f) {
        Partida p = new Partida();
        p.setDescripcion(f.descripcion());
        p.setCantidad(f.cantidad());
        p.setCosto(f.costo());
        p.setPrecioUnitario(f.precioUnitario());
        return p;
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

    /** Copia con fecha de hoy, folio nuevo y en borrador. */
    @Transactional
    public long duplicar(long id) {
        CotizacionForm copia = CotizacionForm.de(obtener(id));
        return guardar(null, new CotizacionForm(LocalDate.now(), copia.vigenciaDias(), copia.cliente(),
                copia.aplicaIva(), copia.tasaIva(), copia.envio(), copia.formaPago(), copia.tiempoEntrega(),
                copia.garantia(), copia.observaciones(), copia.partidas()));
    }

    public void cambiarEstado(long id, Estado estado) {
        obtener(id);
        cotizaciones.cambiarEstado(id, estado);
    }

    public void eliminar(long id) {
        obtener(id);
        cotizaciones.eliminar(id);
    }
}
