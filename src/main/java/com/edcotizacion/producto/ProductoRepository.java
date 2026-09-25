package com.edcotizacion.producto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.edcotizacion.config.Busqueda;

import static com.edcotizacion.cotizacion.Montos.decimal;
import static com.edcotizacion.cotizacion.Montos.texto;

@Repository
public class ProductoRepository {

    private static final RowMapper<Producto> MAPPER = (rs, i) -> {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setUltimoCosto(decimal(rs.getString("ultimo_costo")));
        p.setUltimoPrecio(decimal(rs.getString("ultimo_precio")));
        return p;
    };

    private final JdbcClient jdbc;

    public ProductoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Busca cada palabra por separado, sin acentos: "benq proy" encuentra "Proyector Benq ...". */
    public List<Producto> buscar(String texto, int limite) {
        Busqueda b = new Busqueda(texto);
        return jdbc.sql("SELECT * FROM producto ORDER BY descripcion").query(MAPPER).list().stream()
                .filter(p -> b.coincide(p.getDescripcion()))
                .limit(limite)
                .toList();
    }

    /** Da de alta el producto si no existe y recuerda el último costo y precio. Devuelve el id. */
    public long guardar(String descripcion, BigDecimal costo, BigDecimal precio) {
        jdbc.sql("""
                INSERT INTO producto (descripcion, ultimo_costo, ultimo_precio) VALUES (?, ?, ?)
                ON CONFLICT(descripcion) DO UPDATE SET
                    ultimo_costo  = COALESCE(excluded.ultimo_costo, ultimo_costo),
                    ultimo_precio = excluded.ultimo_precio""")
                .params(descripcion.trim(), texto(costo), texto(precio))
                .update();
        return jdbc.sql("SELECT id FROM producto WHERE descripcion = ?")
                .param(descripcion.trim()).query(Long.class).single();
    }
}
