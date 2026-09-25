package com.edcotizacion.cliente;

import com.edcotizacion.cotizacion.DatosCliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Catálogo de clientes: se da de alta solo al guardar una cotización con un nombre nuevo. */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Único sin distinguir mayúsculas (COLLATE NOCASE en la tabla). */
    @Column(nullable = false, unique = true)
    private String nombre;

    private String contacto;
    private String telefono;
    private String email;
    private String rfc;
    private String direccion;

    protected Cliente() {
        // JPA
    }

    public Cliente(DatosCliente d) {
        this.nombre = d.nombre();
        actualizarCon(d);
    }

    /** Toma solo los datos que vienen capturados; lo vacío no borra lo que ya se tenía. */
    public void actualizarCon(DatosCliente d) {
        if (d.contacto() != null) contacto = d.contacto();
        if (d.telefono() != null) telefono = d.telefono();
        if (d.email() != null) email = d.email();
        if (d.rfc() != null) rfc = d.rfc();
        if (d.direccion() != null) direccion = d.direccion();
    }

    /** Datos para el formulario (autocompletado). */
    public DatosCliente datos() {
        return new DatosCliente(nombre, contacto, telefono, email, rfc, direccion);
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
}
