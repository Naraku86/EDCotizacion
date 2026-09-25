package com.edcotizacion.producto;

import java.math.BigDecimal;

public class Producto {

    private Long id;
    private String descripcion;
    private BigDecimal ultimoCosto;
    private BigDecimal ultimoPrecio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getUltimoCosto() { return ultimoCosto; }
    public void setUltimoCosto(BigDecimal ultimoCosto) { this.ultimoCosto = ultimoCosto; }
    public BigDecimal getUltimoPrecio() { return ultimoPrecio; }
    public void setUltimoPrecio(BigDecimal ultimoPrecio) { this.ultimoPrecio = ultimoPrecio; }
}
