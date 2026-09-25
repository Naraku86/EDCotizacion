package com.edcotizacion.config;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Valores por defecto guardados en la tabla config (clave/valor). */
@Service
public class ConfigService {

    public static final String FOLIO_SIGUIENTE = "folio.siguiente";
    public static final String FOLIO_PREFIJO = "folio.prefijo";
    public static final String IVA_TASA = "iva.tasa";
    public static final String GANANCIA_DEFAULT = "ganancia.default";
    public static final String VIGENCIA_DEFAULT = "vigencia.default";
    public static final String FORMA_PAGO = "condicion.forma_pago";
    public static final String TIEMPO_ENTREGA = "condicion.tiempo_entrega";
    public static final String GARANTIA = "condicion.garantia";
    public static final String OBSERVACIONES = "condicion.observaciones";

    private final JdbcClient jdbc;

    public ConfigService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, String> todos() {
        Map<String, String> m = new LinkedHashMap<>();
        jdbc.sql("SELECT clave, valor FROM config").query(rs -> {
            m.put(rs.getString(1), rs.getString(2));
        });
        return m;
    }

    public String get(String clave) {
        return jdbc.sql("SELECT valor FROM config WHERE clave = ?")
                .param(clave).query(String.class).optional().orElse("");
    }

    public BigDecimal getDecimal(String clave) {
        String v = get(clave);
        return v.isBlank() ? BigDecimal.ZERO : new BigDecimal(v.trim());
    }

    public int getInt(String clave) {
        String v = get(clave);
        return v.isBlank() ? 0 : Integer.parseInt(v.trim());
    }

    public void set(String clave, String valor) {
        jdbc.sql("INSERT INTO config (clave, valor) VALUES (?, ?) "
                + "ON CONFLICT(clave) DO UPDATE SET valor = excluded.valor")
                .params(clave, valor).update();
    }

    /** Devuelve el siguiente folio libre (p. ej. COT-0003) y avanza el contador. */
    @Transactional
    public String tomarFolio() {
        int n = getInt(FOLIO_SIGUIENTE);
        String folio;
        do {
            folio = get(FOLIO_PREFIJO) + String.format("%04d", n++);
        } while (jdbc.sql("SELECT COUNT(*) FROM cotizacion WHERE folio = ?")
                .param(folio).query(Integer.class).single() > 0);
        set(FOLIO_SIGUIENTE, String.valueOf(n));
        return folio;
    }
}
