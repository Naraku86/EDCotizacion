package com.edcotizacion.cotizacion;

import static com.edcotizacion.cotizacion.Montos.decimal;
import static com.edcotizacion.cotizacion.Montos.texto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.edcotizacion.comun.Busqueda;

@Repository
public class CotizacionRepository {

    private final JdbcClient jdbc;

    public CotizacionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Cotizacion> porId(long id) {
        Optional<Cotizacion> cot = jdbc.sql("SELECT * FROM cotizacion WHERE id = ?")
                .param(id).query((rs, i) -> mapear(rs)).optional();
        cot.ifPresent(c -> c.setPartidas(partidas(id)));
        return cot;
    }

    /** Lista para la pantalla principal (sin partidas). Busca en folio, cliente y productos. */
    public List<Cotizacion> buscar(String texto, Estado estado) {
        List<Cotizacion> todas = jdbc.sql("SELECT * FROM cotizacion WHERE ?1 IS NULL OR estado = ?1 ORDER BY id DESC")
                .param(1, estado == null ? null : estado.name())
                .query((rs, i) -> mapear(rs)).list();
        if (texto == null || texto.isBlank()) {
            return todas;
        }
        Map<Long, String> productos = new HashMap<>();
        jdbc.sql("SELECT cotizacion_id, group_concat(descripcion, ' ') FROM partida GROUP BY cotizacion_id")
                .query(rs -> {
                    productos.put(rs.getLong(1), rs.getString(2));
                });
        Busqueda b = new Busqueda(texto);
        return todas.stream()
                .filter(c -> b.coincide(c.getFolio() + " " + c.getCliente().nombre() + " "
                        + Objects.toString(c.getCliente().contacto(), "") + " " + productos.getOrDefault(c.getId(), "")))
                .toList();
    }

    public long insertar(Cotizacion c) {
        jdbc.sql("""
                INSERT INTO cotizacion (folio, fecha, vigencia_dias, estado, cliente_id,
                    cliente_nombre, cliente_contacto, cliente_telefono, cliente_email, cliente_rfc, cliente_direccion,
                    aplica_iva, tasa_iva, envio, subtotal, iva, total,
                    forma_pago, tiempo_entrega, garantia, observaciones, creada, modificada)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""")
                .params(c.getFolio(), c.getFecha().toString(), c.getVigenciaDias(), c.getEstado().name(),
                        c.getClienteId(), c.getCliente().nombre(), c.getCliente().contacto(),
                        c.getCliente().telefono(), c.getCliente().email(), c.getCliente().rfc(),
                        c.getCliente().direccion(),
                        c.isAplicaIva() ? 1 : 0, texto(c.getTasaIva()), texto(c.getEnvio()),
                        texto(c.getSubtotal()), texto(c.getIva()), texto(c.getTotal()),
                        c.getFormaPago(), c.getTiempoEntrega(), c.getGarantia(), c.getObservaciones(),
                        c.getCreada().toString(), c.getModificada().toString())
                .update();
        long id = jdbc.sql("SELECT last_insert_rowid()").query(Long.class).single();
        insertarPartidas(id, c.getPartidas());
        return id;
    }

    public void actualizar(Cotizacion c) {
        jdbc.sql("""
                UPDATE cotizacion SET fecha = ?, vigencia_dias = ?, cliente_id = ?,
                    cliente_nombre = ?, cliente_contacto = ?, cliente_telefono = ?, cliente_email = ?,
                    cliente_rfc = ?, cliente_direccion = ?,
                    aplica_iva = ?, tasa_iva = ?, envio = ?, subtotal = ?, iva = ?, total = ?,
                    forma_pago = ?, tiempo_entrega = ?, garantia = ?, observaciones = ?, modificada = ?
                WHERE id = ?""")
                .params(c.getFecha().toString(), c.getVigenciaDias(), c.getClienteId(),
                        c.getCliente().nombre(), c.getCliente().contacto(), c.getCliente().telefono(),
                        c.getCliente().email(), c.getCliente().rfc(), c.getCliente().direccion(),
                        c.isAplicaIva() ? 1 : 0, texto(c.getTasaIva()), texto(c.getEnvio()),
                        texto(c.getSubtotal()), texto(c.getIva()), texto(c.getTotal()),
                        c.getFormaPago(), c.getTiempoEntrega(), c.getGarantia(), c.getObservaciones(),
                        c.getModificada().toString(), c.getId())
                .update();
        jdbc.sql("DELETE FROM partida WHERE cotizacion_id = ?").param(c.getId()).update();
        insertarPartidas(c.getId(), c.getPartidas());
    }

    public void cambiarEstado(long id, Estado estado) {
        jdbc.sql("UPDATE cotizacion SET estado = ?, modificada = ? WHERE id = ?")
                .params(estado.name(), LocalDateTime.now().toString(), id).update();
    }

    public void eliminar(long id) {
        jdbc.sql("DELETE FROM partida WHERE cotizacion_id = ?").param(id).update();
        jdbc.sql("DELETE FROM cotizacion WHERE id = ?").param(id).update();
    }

    private void insertarPartidas(long cotizacionId, List<Partida> partidas) {
        int orden = 1;
        for (Partida p : partidas) {
            jdbc.sql("""
                    INSERT INTO partida (cotizacion_id, orden, producto_id, descripcion, cantidad,
                        costo, pct_ganancia, precio_unitario, importe)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""")
                    .params(cotizacionId, orden++, p.getProductoId(), p.getDescripcion(),
                            texto(p.getCantidad()), texto(p.getCosto()), texto(p.getPctGanancia()),
                            texto(p.getPrecioUnitario()), texto(p.getImporte()))
                    .update();
        }
    }

    private List<Partida> partidas(long cotizacionId) {
        return jdbc.sql("SELECT * FROM partida WHERE cotizacion_id = ? ORDER BY orden")
                .param(cotizacionId)
                .query((rs, i) -> {
                    Partida p = new Partida();
                    long productoId = rs.getLong("producto_id");
                    p.setProductoId(rs.wasNull() ? null : productoId);
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setCantidad(decimal(rs.getString("cantidad")));
                    p.setCosto(decimal(rs.getString("costo")));
                    p.setPctGanancia(decimal(rs.getString("pct_ganancia")));
                    p.setPrecioUnitario(decimal(rs.getString("precio_unitario")));
                    p.setImporte(decimal(rs.getString("importe")));
                    return p;
                }).list();
    }

    private static Cotizacion mapear(ResultSet rs) throws SQLException {
        Cotizacion c = new Cotizacion();
        c.setId(rs.getLong("id"));
        c.setFolio(rs.getString("folio"));
        c.setFecha(LocalDate.parse(rs.getString("fecha")));
        c.setVigenciaDias(rs.getInt("vigencia_dias"));
        c.setEstado(Estado.valueOf(rs.getString("estado")));
        long clienteId = rs.getLong("cliente_id");
        c.setClienteId(rs.wasNull() ? null : clienteId);
        c.setCliente(new DatosCliente(rs.getString("cliente_nombre"), rs.getString("cliente_contacto"),
                rs.getString("cliente_telefono"), rs.getString("cliente_email"), rs.getString("cliente_rfc"),
                rs.getString("cliente_direccion")));
        c.setAplicaIva(rs.getInt("aplica_iva") == 1);
        c.setTasaIva(decimal(rs.getString("tasa_iva")));
        c.setEnvio(decimal(rs.getString("envio")));
        c.setSubtotal(decimal(rs.getString("subtotal")));
        c.setIva(decimal(rs.getString("iva")));
        c.setTotal(decimal(rs.getString("total")));
        c.setFormaPago(rs.getString("forma_pago"));
        c.setTiempoEntrega(rs.getString("tiempo_entrega"));
        c.setGarantia(rs.getString("garantia"));
        c.setObservaciones(rs.getString("observaciones"));
        c.setCreada(LocalDateTime.parse(rs.getString("creada")));
        c.setModificada(LocalDateTime.parse(rs.getString("modificada")));
        return c;
    }
}
