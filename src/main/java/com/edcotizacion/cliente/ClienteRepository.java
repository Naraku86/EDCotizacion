package com.edcotizacion.cliente;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.edcotizacion.config.Busqueda;

@Repository
public class ClienteRepository {

    private static final RowMapper<Cliente> MAPPER = (rs, i) -> {
        Cliente c = new Cliente();
        c.setId(rs.getLong("id"));
        c.setNombre(rs.getString("nombre"));
        c.setContacto(rs.getString("contacto"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setRfc(rs.getString("rfc"));
        c.setDireccion(rs.getString("direccion"));
        return c;
    };

    private final JdbcClient jdbc;

    public ClienteRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Cliente> buscar(String texto, int limite) {
        Busqueda b = new Busqueda(texto);
        return jdbc.sql("SELECT * FROM cliente ORDER BY nombre").query(MAPPER).list().stream()
                .filter(c -> b.coincide(c.getNombre() + " " + Objects.toString(c.getContacto(), "")))
                .limit(limite)
                .toList();
    }

    public Optional<Cliente> porNombre(String nombre) {
        return jdbc.sql("SELECT * FROM cliente WHERE nombre = ?")
                .param(nombre.trim()).query(MAPPER).optional();
    }

    /**
     * Da de alta el cliente si no existe; si existe actualiza solo los datos
     * que vienen capturados. Devuelve el id.
     */
    public long guardar(Cliente c) {
        Optional<Cliente> existente = porNombre(c.getNombre());
        if (existente.isEmpty()) {
            jdbc.sql("INSERT INTO cliente (nombre, contacto, telefono, email, rfc, direccion) VALUES (?, ?, ?, ?, ?, ?)")
                    .params(c.getNombre().trim(), c.getContacto(), c.getTelefono(), c.getEmail(), c.getRfc(), c.getDireccion())
                    .update();
            return jdbc.sql("SELECT last_insert_rowid()").query(Long.class).single();
        }
        long id = existente.get().getId();
        jdbc.sql("""
                UPDATE cliente SET
                    contacto  = COALESCE(NULLIF(?, ''), contacto),
                    telefono  = COALESCE(NULLIF(?, ''), telefono),
                    email     = COALESCE(NULLIF(?, ''), email),
                    rfc       = COALESCE(NULLIF(?, ''), rfc),
                    direccion = COALESCE(NULLIF(?, ''), direccion)
                WHERE id = ?""")
                .params(c.getContacto(), c.getTelefono(), c.getEmail(), c.getRfc(), c.getDireccion(), id)
                .update();
        return id;
    }
}
