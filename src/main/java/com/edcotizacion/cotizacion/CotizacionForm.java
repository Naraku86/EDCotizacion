package com.edcotizacion.cotizacion;

import static com.edcotizacion.comun.Textos.limpio;
import static com.edcotizacion.comun.Textos.vacio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Lo que captura el formulario de cotización. Solo trae lo que el usuario puede cambiar:
 * folio, estado, totales y fechas internas los pone el servidor.
 */
public record CotizacionForm(
        @NotNull(message = "Elige la empresa emisora.")
        @Positive(message = "La empresa emisora no es válida.") Long empresaId,
        @NotNull(message = "Falta la fecha.") LocalDate fecha,
        @PositiveOrZero(message = "La vigencia no puede ser negativa.")
        @Max(value = 3650, message = "La vigencia es demasiado larga.") int vigenciaDias,
        @NotNull(message = "Faltan los datos del cliente.") @Valid DatosCliente cliente,
        boolean aplicaIva,
        @NotNull(message = "Falta la tasa de IVA.")
        @PositiveOrZero(message = "La tasa de IVA no puede ser negativa.")
        @DecimalMax(value = "100", message = "La tasa de IVA no puede pasar de 100%.") BigDecimal tasaIva,
        @PositiveOrZero(message = "El envío no puede ser negativo.") BigDecimal envio,
        @Size(max = 1000, message = "La forma de pago es demasiado larga.") String formaPago,
        @Size(max = 1000, message = "El tiempo de entrega es demasiado largo.") String tiempoEntrega,
        @Size(max = 1000, message = "La garantía es demasiado larga.") String garantia,
        @Size(max = 2000, message = "Las observaciones son demasiado largas.") String observaciones,
        @NotEmpty(message = "Agrega al menos un producto.")
        @Size(max = 500, message = "Demasiadas partidas.") List<@Valid @NotNull PartidaForm> partidas) {

    /** Renglón de productos. pctGanancia solo se usa para mostrarlo; el servidor lo recalcula. */
    public record PartidaForm(
            @NotNull(message = "falta la descripción.")
            @Size(max = 1000, message = "la descripción es demasiado larga.") String descripcion,
            @NotNull(message = "falta la cantidad.")
            @Positive(message = "la cantidad debe ser mayor a 0.") BigDecimal cantidad,
            @PositiveOrZero(message = "el costo no puede ser negativo.") BigDecimal costo,
            @NotNull(message = "falta el precio.")
            @PositiveOrZero(message = "el precio no puede ser negativo.") BigDecimal precioUnitario,
            BigDecimal pctGanancia) {

        public PartidaForm {
            descripcion = limpio(descripcion);
        }

        /** Renglón que el usuario dejó en blanco: se ignora. */
        public boolean enBlanco() {
            return vacio(descripcion) && precioUnitario == null;
        }
    }

    public CotizacionForm {
        cliente = cliente == null ? DatosCliente.vacio() : cliente;
        envio = envio == null ? BigDecimal.ZERO : envio;
        // las condiciones vacías no se imprimen en el PDF
        formaPago = limpio(formaPago);
        tiempoEntrega = limpio(tiempoEntrega);
        garantia = limpio(garantia);
        observaciones = limpio(observaciones);
        partidas = partidas == null ? List.of()
                : partidas.stream().filter(p -> p == null || !p.enBlanco()).toList();
    }

    /** Para editar o duplicar una cotización existente. */
    public static CotizacionForm de(Cotizacion c) {
        return new CotizacionForm(c.getEmpresa().getId(), c.getFecha(), c.getVigenciaDias(), c.getCliente(), c.isAplicaIva(),
                c.getTasaIva(), c.getEnvio(), c.getFormaPago(), c.getTiempoEntrega(), c.getGarantia(),
                c.getObservaciones(),
                c.getPartidas().stream()
                        .map(p -> new PartidaForm(p.getDescripcion(), p.getCantidad(), p.getCosto(),
                                p.getPrecioUnitario(), p.getPctGanancia()))
                        .toList());
    }
}
