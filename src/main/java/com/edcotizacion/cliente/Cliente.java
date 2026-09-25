package com.edcotizacion.cliente;

import com.edcotizacion.cotizacion.DatosCliente;

public class Cliente {

    private Long id;
    private String nombre;
    private String contacto;
    private String telefono;
    private String email;
    private String rfc;
    private String direccion;

    /** Datos para el formulario (autocompletado). */
    public DatosCliente datos() {
        return new DatosCliente(nombre, contacto, telefono, email, rfc, direccion);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
