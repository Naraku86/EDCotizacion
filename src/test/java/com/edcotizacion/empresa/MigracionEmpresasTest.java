package com.edcotizacion.empresa;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class MigracionEmpresasTest {
    @TempDir Path carpeta;

    @Test
    void migraDatosLogoPlantillaYCotizacionesAnteriores() throws Exception {
        String url = "jdbc:sqlite:" + carpeta.resolve("anterior.db") + "?foreign_keys=true";
        Flyway.configure().dataSource(url, null, null).target("4").load().migrate();
        String diseno = "{\"plantilla\":\"moderna\",\"textos\":{\"titulo\":\"PRESUPUESTO ANTERIOR\"}}";
        try (var cn = DriverManager.getConnection(url); var sql = cn.createStatement()) {
            sql.executeUpdate("UPDATE config SET valor = 'Empresa anterior' WHERE clave = 'empresa.nombre'");
            sql.executeUpdate("UPDATE config SET valor = 'data:image/png;base64,YWJj' WHERE clave = 'empresa.logo'");
            try (var insert = cn.prepareStatement("INSERT INTO config(clave, valor) VALUES ('plantilla.diseno', ?)")) {
                insert.setString(1, diseno);
                insert.executeUpdate();
            }
            sql.executeUpdate("""
                INSERT INTO cotizacion(folio,fecha,vigencia_dias,cliente_nombre,tasa_iva,subtotal,iva,total,creada,modificada)
                VALUES('COT-0099','2026-09-25',15,'Cliente anterior','16','100','16','116','2026-09-25T10:00','2026-09-25T10:00')
                """);
        }
        Flyway.configure().dataSource(url, null, null).load().migrate();
        try (var cn = DriverManager.getConnection(url); var sql = cn.createStatement()) {
            try (var rs = sql.executeQuery("SELECT * FROM empresa")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getLong("id"));
                assertEquals("Empresa anterior", rs.getString("nombre"));
                assertEquals(diseno, rs.getString("diseno"));
                var datos = JsonMapper.builder().build().readTree(rs.getString("datos"));
                assertEquals("Empresa anterior", datos.get("nombre").asText());
                assertEquals("data:image/png;base64,YWJj", datos.get("logo").asText());
                assertFalse(rs.next());
            }
            try (var rs = sql.executeQuery("SELECT empresa_id, folio, total FROM cotizacion")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getLong("empresa_id"));
                assertEquals("COT-0099", rs.getString("folio"));
                assertEquals("116", rs.getString("total"));
            }
            try (var rs = sql.executeQuery("PRAGMA foreign_key_check")) { assertFalse(rs.next()); }
        }
    }
}
