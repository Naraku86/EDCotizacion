package com.edcotizacion.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edcotizacion.config.ConfigService;

import tools.jackson.databind.json.JsonMapper;

/**
 * Diseño del PDF (bloques en orden + colores + pie) y datos de la empresa.
 * Ambos viven en la tabla config; si nunca se ha guardado un diseño se usa el
 * genérico incluido en el jar (pdf/diseno-generico.json).
 */
@Service
public class DisenoService {

    public static final String DISENO = "plantilla.diseno";
    private static final String EMPRESA = "empresa.";

    private final ConfigService config;
    private final JsonMapper json;

    public DisenoService(ConfigService config, JsonMapper json) {
        this.config = config;
        this.json = json;
    }

    public Map<String, Object> diseno() {
        String v = config.get(DISENO);
        return v.isBlank() ? generico() : leer(v);
    }

    public Map<String, Object> generico() {
        try (InputStream in = getClass().getResourceAsStream("/pdf/diseno-generico.json")) {
            return leer(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Empresa empresa() {
        Map<String, String> m = new HashMap<>();
        for (String campo : Empresa.CAMPOS) {
            String v = config.get(EMPRESA + campo).trim();
            m.put(campo, v.isEmpty() ? null : v);
        }
        return json.convertValue(m, Empresa.class);
    }

    @Transactional
    public void guardar(Map<String, Object> diseno, Empresa empresa) {
        if (diseno == null || !(diseno.get("bloques") instanceof List)) {
            throw new IllegalArgumentException("El diseño no tiene bloques");
        }
        config.set(DISENO, json.writeValueAsString(diseno));
        Map<?, ?> m = json.convertValue(empresa, Map.class);
        for (String campo : Empresa.CAMPOS) {
            config.set(EMPRESA + campo, Objects.toString(m.get(campo), "").trim());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> leer(String v) {
        return json.readValue(v, Map.class);
    }
}
