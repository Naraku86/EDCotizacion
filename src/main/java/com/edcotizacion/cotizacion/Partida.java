package com.edcotizacion.cotizacion;

import java.math.BigDecimal;

public class Partida {

    private Long productoId;
    private String descripcion;
    private BigDecimal cantidad;
    /** Costo y % de ganancia son internos: no salen en el PDF. */
    private BigDecimal costo;
    private BigDecimal pctGanancia;
    private BigDecimal precioUnitario;
    private BigDecimal importe;

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
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    public BigDecimal getPctGanancia() { return pctGanancia; }
    public void setPctGanancia(BigDecimal pctGanancia) { this.pctGanancia = pctGanancia; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
}
