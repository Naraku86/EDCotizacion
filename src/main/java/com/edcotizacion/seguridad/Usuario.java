package com.edcotizacion.seguridad;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Único sin distinguir mayúsculas (COLLATE NOCASE en la tabla). */
    @Column(nullable = false, unique = true)
    private String nombre;

    /** Hash con prefijo del algoritmo, p. ej. {bcrypt}$2a$10$... Nunca la contraseña en claro. */
    @Column(nullable = false)
    private String password;

    /** true mientras siga con la contraseña de fábrica (admin/admin). */
    @Column(name = "por_defecto", nullable = false)
    private boolean porDefecto;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    protected Usuario() {
        // JPA
    }

    public Usuario(String nombre, String passwordHash, boolean porDefecto) {
        this.nombre = nombre;
        this.password = passwordHash;
        this.porDefecto = porDefecto;
    }

    public void cambiar(String nombre, String passwordHash) {
        this.nombre = nombre;
        this.password = passwordHash;
        this.porDefecto = false;
    }

    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    /** Cuenta un intento fallido; al llegar al máximo bloquea la cuenta por un tiempo. */
    public void registrarFallo(LocalDateTime ahora, int maximo, Duration bloqueo) {
        intentosFallidos++;
        if (intentosFallidos >= maximo) {
            bloqueadoHasta = ahora.plus(bloqueo);
            intentosFallidos = 0;
        }
    }

    public void registrarExito() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
    public boolean isPorDefecto() { return porDefecto; }
}
