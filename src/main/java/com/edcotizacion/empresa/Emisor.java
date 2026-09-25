package com.edcotizacion.empresa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Empresa emisora de cotizaciones. Sus datos (nombre, RFC, logo…) y su diseño de PDF se
 * guardan como JSON; quien los interpreta es DisenoService.
 */
@Entity
@Table(name = "empresa")
public class Emisor {

    public static final int NOMBRE_MAXIMO = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NOMBRE_MAXIMO)
    private String nombre;

    @Column(nullable = false)
    private String datos;

    @Column(nullable = false)
    private String diseno;

    protected Emisor() {
    }

    public Emisor(String nombre, String datos, String diseno) {
        actualizar(nombre, datos, diseno);
    }

    /** Reemplaza nombre, datos y diseño juntos, para que el nombre y los datos no se desincronicen. */
    public void actualizar(String nombre, String datos, String diseno) {
        String n = nombreValido(nombre);
        requerido(datos);
        requerido(diseno);
        this.nombre = n;
        this.datos = datos;
        this.diseno = diseno;
    }

    /** Nombre sin espacios sobrantes; lanza IllegalArgumentException si está vacío o es muy largo. */
    public static String nombreValido(String nombre) {
        String n = nombre == null ? "" : nombre.trim();
        if (n.isEmpty() || n.length() > NOMBRE_MAXIMO) {
            throw new IllegalArgumentException(
                    "Captura un nombre de empresa de 1 a " + NOMBRE_MAXIMO + " caracteres.");
        }
        return n;
    }

    private static void requerido(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Faltan los datos de la empresa.");
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDatos() { return datos; }
    public String getDiseno() { return diseno; }
}
