package com.edcotizacion.seguridad;

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

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
    public boolean isPorDefecto() { return porDefecto; }
}
