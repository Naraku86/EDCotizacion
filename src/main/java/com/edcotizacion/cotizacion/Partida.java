package com.edcotizacion.cotizacion;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Renglón de una cotización. Se guarda y se borra junto con su cotización. */
@Entity
@Table(name = "partida")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cotizacion_id", nullable = false)
    private Cotizacion cotizacion;

    @Column(nullable = false)
    private int orden;

    @Column(name = "producto_id")
    private Long productoId;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal cantidad;

    /** Costo y % de ganancia son internos: no salen en el PDF. */
    private BigDecimal costo;

    @Column(name = "pct_ganancia")
    private BigDecimal pctGanancia;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private BigDecimal importe;

    protected Partida() {
        // JPA
    }

    public Partida(String descripcion, BigDecimal cantidad, BigDecimal costo, BigDecimal precioUnitario) {
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.costo = costo;
        this.precioUnitario = precioUnitario;
    }

    void asignar(Cotizacion cotizacion, int orden) {
        this.cotizacion = cotizacion;
        this.orden = orden;
    }

    /** Ganancia interna de la partida (null si no se capturó costo). */
    public BigDecimal getGanancia() {
        if (costo == null || precioUnitario == null || cantidad == null) {
            return null;
        }
        return Montos.r2(precioUnitario.subtract(costo).multiply(cantidad));
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    public BigDecimal getPctGanancia() { return pctGanancia; }
    public void setPctGanancia(BigDecimal pctGanancia) { this.pctGanancia = pctGanancia; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
}
