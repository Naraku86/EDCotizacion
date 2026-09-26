package com.edcotizacion.demo;

import static com.edcotizacion.comun.Textos.limpio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.edcotizacion.cotizacion.Cotizacion;
import com.edcotizacion.cotizacion.CotizacionForm.PartidaForm;
import com.edcotizacion.cotizacion.CotizacionService;
import com.edcotizacion.cotizacion.DatosCliente;
import com.edcotizacion.cotizacion.Partida;
import com.edcotizacion.pdf.Empresa;

/**
 * Todo lo que captura la página del demo: la empresa, el diseño elegido y la cotización.
 * Nunca se guarda; solo sirve para armar la vista previa y el PDF en memoria.
 */
public record DemoForm(
        @NotNull(message = "Faltan los datos de tu empresa.") @Valid Empresa empresa,
        @NotNull(message = "Elige un diseño.") @Valid DisenoDemo diseno,
        @NotBlank(message = "Captura el folio.")
        @Size(max = 30, message = "El folio es demasiado largo.") String folio,
        @NotNull(message = "Falta la fecha.") LocalDate fecha,
        @PositiveOrZero(message = "La vigencia no puede ser negativa.")
        @Max(value = 3650, message = "La vigencia es demasiado larga.") int vigenciaDias,
        @NotNull(message = "Faltan los datos del cliente.") @Valid DatosCliente cliente,
        boolean aplicaIva,
        @NotNull(message = "Falta la tasa de IVA.")
        @PositiveOrZero(message = "La tasa de IVA no puede ser negativa.")
        @DecimalMax(value = "100", message = "La tasa de IVA no puede pasar de 100%.")
        @Digits(integer = 3, fraction = 4, message = "La tasa de IVA tiene demasiados decimales.") BigDecimal tasaIva,
        @PositiveOrZero(message = "El envío no puede ser negativo.")
        @Digits(integer = 12, fraction = 4, message = "El envío es demasiado grande o tiene demasiados decimales.") BigDecimal envio,
        @Size(max = 1000, message = "La forma de pago es demasiado larga.") String formaPago,
        @Size(max = 1000, message = "El tiempo de entrega es demasiado largo.") String tiempoEntrega,
        @Size(max = 1000, message = "La garantía es demasiado larga.") String garantia,
        @Size(max = 2000, message = "Las observaciones son demasiado largas.") String observaciones,
        @NotEmpty(message = "Agrega al menos un producto.")
        @Size(max = DemoForm.MAX_PARTIDAS, message = "El demo admite hasta " + DemoForm.MAX_PARTIDAS + " productos.")
        List<@Valid @NotNull PartidaForm> partidas) {

    /** Menos que en el sistema (500): la página es pública y cada PDF cuesta CPU. */
    public static final int MAX_PARTIDAS = 50;

    /** Diseño elegido de la galería: plantilla, dos colores y tamaño de papel. */
    public record DisenoDemo(
            String plantilla,
            @Pattern(regexp = "#[0-9a-fA-F]{6}", message = "El color principal no es válido.")
            String primario,
            @Pattern(regexp = "#[0-9a-fA-F]{6}", message = "El color de acento no es válido.")
            String acento,
            String papel) {
    }

    public DemoForm {
        folio = limpio(folio);
        envio = envio == null ? BigDecimal.ZERO : envio;
        formaPago = limpio(formaPago);
        tiempoEntrega = limpio(tiempoEntrega);
        garantia = limpio(garantia);
        observaciones = limpio(observaciones);
        partidas = partidas == null ? List.of()
                : partidas.stream().filter(p -> p == null || !p.enBlanco()).toList();
    }

    /** Diseño para DisenoService.normalizar: lo elegido y, en el pie, el nombre de la empresa. */
    public Map<String, Object> disenoCrudo() {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("plantilla", diseno.plantilla());
        d.put("papel", diseno.papel());
        Map<String, Object> colores = new LinkedHashMap<>();
        colores.put("primario", diseno.primario());
        colores.put("acento", diseno.acento());
        d.put("colores", colores);
        d.put("textos", Map.of("pie", empresa.nombre() == null ? "" : empresa.nombre()));
        return d;
    }

    /** Cotización en memoria (sin id ni empresa emisora) con importes y totales calculados. */
    public Cotizacion cotizacion() {
        Cotizacion c = new Cotizacion();
        c.setFolio(folio);
        c.setFecha(fecha);
        c.setVigenciaDias(vigenciaDias);
        c.setCliente(cliente);
        c.setAplicaIva(aplicaIva);
        c.setTasaIva(tasaIva);
        c.setEnvio(envio);
        c.setFormaPago(formaPago);
        c.setTiempoEntrega(tiempoEntrega);
        c.setGarantia(garantia);
        c.setObservaciones(observaciones);
        c.reemplazarPartidas(partidas.stream()
                .map(p -> new Partida(p.descripcion(), p.cantidad(), null, p.precioUnitario()))
                .toList());
        CotizacionService.calcular(c);
        return c;
    }
}
