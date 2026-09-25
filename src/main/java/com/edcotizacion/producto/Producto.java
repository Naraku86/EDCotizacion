package com.edcotizacion.producto;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Catálogo de productos: se da de alta solo y recuerda el último costo y precio usados. */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Única sin distinguir mayúsculas (COLLATE NOCASE en la tabla). */
    @Column(nullable = false, unique = true)
    private String descripcion;

    @Column(name = "ultimo_costo")
    private BigDecimal ultimoCosto;

    @Column(name = "ultimo_precio")
    private BigDecimal ultimoPrecio;

    protected Producto() {
        // JPA
    }

    public Producto(String descripcion) {
        this.descripcion = descripcion;
    }

    /** El costo solo se reemplaza si se capturó; el precio siempre. */
    public void recordarPrecios(BigDecimal costo, BigDecimal precio) {
        if (costo != null) {
            ultimoCosto = costo;
        }
        ultimoPrecio = precio;
    }

    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getUltimoCosto() { return ultimoCosto; }
    public BigDecimal getUltimoPrecio() { return ultimoPrecio; }
}
