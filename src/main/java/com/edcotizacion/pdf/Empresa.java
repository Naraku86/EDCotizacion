package com.edcotizacion.pdf;

import java.util.List;

/** Datos del proveedor que imprime el PDF. Se guardan en la tabla config como empresa.*. */
public class Empresa {

    public static final List<String> CAMPOS = List.of(
            "nombre", "lema", "rfc", "telefono", "correo", "direccion", "web", "ejecutivo", "logo");

    private String nombre;
    private String lema;
    private String rfc;
    private String telefono;
    private String correo;
    private String direccion;
    private String web;
    private String ejecutivo;
    /** Imagen como data URI (data:image/png;base64,...). */
    private String logo;

    /** Campos vacíos -> null, para que la plantilla no imprima "RFC:" sin valor. */
    public Empresa limpia() {
        nombre = nulo(nombre);
        lema = nulo(lema);
        rfc = nulo(rfc);
        telefono = nulo(telefono);
        correo = nulo(correo);
        direccion = nulo(direccion);
        web = nulo(web);
        ejecutivo = nulo(ejecutivo);
        logo = nulo(logo);
        return this;
    }

    private static String nulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    /** "EV Soluciones" -> 0: "EV", 1: "Soluciones". Para el nombre en dos colores. */
    public String nombreParte(int i) {
        String n = nombre == null ? "" : nombre.trim();
        int esp = n.indexOf(' ');
        if (esp < 0) {
            return i == 0 ? n : "";
        }
        return i == 0 ? n.substring(0, esp) : n.substring(esp + 1).trim();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getLema() { return lema; }
    public void setLema(String lema) { this.lema = lema; }
    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web; }
    public String getEjecutivo() { return ejecutivo; }
    public void setEjecutivo(String ejecutivo) { this.ejecutivo = ejecutivo; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
}
