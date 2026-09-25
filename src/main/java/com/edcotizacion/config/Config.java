package com.edcotizacion.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Un valor de configuración (clave/valor). */
@Entity
@Table(name = "config")
public class Config {

    @Id
    private String clave;

    private String valor;

    protected Config() {
        // JPA
    }

    public Config(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public String getClave() { return clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
