package com.edcotizacion.config;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edcotizacion.cotizacion.CotizacionRepository;

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

    private final ConfigRepository repo;
    private final CotizacionRepository cotizaciones;

    public ConfigService(ConfigRepository repo, CotizacionRepository cotizaciones) {
        this.repo = repo;
        this.cotizaciones = cotizaciones;
    }

    @Transactional(readOnly = true)
    public Map<String, String> todos() {
        Map<String, String> m = new LinkedHashMap<>();
        repo.findAll().forEach(c -> m.put(c.getClave(), c.getValor()));
        return m;
    }

    @Transactional(readOnly = true)
    public String get(String clave) {
        return repo.findById(clave).map(Config::getValor).orElse("");
    }

    public BigDecimal getDecimal(String clave) {
        String v = get(clave);
        return v.isBlank() ? BigDecimal.ZERO : new BigDecimal(v.trim());
    }

    public int getInt(String clave) {
        String v = get(clave);
        return v.isBlank() ? 0 : Integer.parseInt(v.trim());
    }

    @Transactional
    public void set(String clave, String valor) {
        repo.findById(clave).ifPresentOrElse(c -> c.setValor(valor), () -> repo.save(new Config(clave, valor)));
    }

    /** Devuelve el siguiente folio libre (p. ej. COT-0003) y avanza el contador. */
    @Transactional
    public String tomarFolio() {
        int n = getInt(FOLIO_SIGUIENTE);
        String prefijo = get(FOLIO_PREFIJO);
        String folio;
        do {
            folio = prefijo + String.format("%04d", n++);
        } while (cotizaciones.existsByFolio(folio));
        set(FOLIO_SIGUIENTE, String.valueOf(n));
        return folio;
    }
}
