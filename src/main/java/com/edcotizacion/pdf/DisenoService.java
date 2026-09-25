package com.edcotizacion.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edcotizacion.config.ConfigService;

import tools.jackson.databind.json.JsonMapper;

/**
 * Diseño del PDF y datos de la empresa. Ambos viven en la tabla config; si nunca se ha
 * guardado un diseño se usa el genérico incluido en el jar (pdf/diseno-generico.json).
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

    public Diseno diseno() {
        String v = config.get(DISENO);
        return normalizar(v.isBlank() ? Map.of() : leer(v));
    }

    public Map<String, Object> generico() {
        return leer(recurso("/pdf/diseno-generico.json"));
    }

    /** Galería de ejemplos: nombre, descripción, plantilla y colores. */
    public List<?> ejemplos() {
        return json.readValue(recurso("/pdf/ejemplos.json"), List.class);
    }

    /**
     * Completa lo que venga del editor (o de la base) con el diseño genérico y lo valida.
     * También entiende el formato anterior por bloques y lo convierte.
     */
    public Diseno normalizar(Map<String, Object> crudo) {
        Map<String, Object> d = crudo == null ? Map.of() : crudo;
        if (d.containsKey("bloques")) {
            d = desdeBloques(d);
        }
        return json.convertValue(mezclar(generico(), d), Diseno.class);
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
        config.set(DISENO, json.writeValueAsString(normalizar(diseno)));
        Map<?, ?> m = json.convertValue(empresa, Map.class);
        for (String campo : Empresa.CAMPOS) {
            config.set(EMPRESA + campo, Objects.toString(m.get(campo), "").trim());
        }
    }

    /** Copia profunda de base con los valores de encima (los mapas se mezclan, lo demás se reemplaza). */
    @SuppressWarnings("unchecked")
    static Map<String, Object> mezclar(Map<String, Object> base, Map<String, Object> encima) {
        Map<String, Object> r = new LinkedHashMap<>(base);
        encima.forEach((k, v) -> {
            if (v instanceof Map<?, ?> mv && r.get(k) instanceof Map<?, ?> mb) {
                r.put(k, mezclar((Map<String, Object>) mb, (Map<String, Object>) mv));
            } else if (v != null) {
                r.put(k, v);
            }
        });
        return r;
    }

    /** Diseño del editor anterior (lista de bloques) -> diseño actual con la plantilla clásica. */
    private static Map<String, Object> desdeBloques(Map<String, Object> viejo) {
        Map<String, Object> n = new LinkedHashMap<>();
        Map<String, Object> textos = new LinkedHashMap<>();
        n.put("plantilla", "clasica");
        n.put("textos", textos);
        if (viejo.get("estilo") instanceof Map<?, ?> est) {
            Map<String, Object> colores = new LinkedHashMap<>();
            colores.put("primario", est.get("primario"));
            colores.put("acento", est.get("acento"));
            n.put("colores", colores);
        }
        if (viejo.get("pie") instanceof Map<?, ?> pie) {
            textos.put("pie", pie.get("texto"));
            n.put("paginas", Boolean.TRUE.equals(pie.get("paginas")));
        }
        if (viejo.get("bloques") instanceof List<?> bloques) {
            for (Object o : bloques) {
                if (!(o instanceof Map<?, ?> b)) {
                    continue;
                }
                switch (String.valueOf(b.get("tipo"))) {
                    case "encabezado" -> {
                        textos.put("titulo", b.get("titulo"));
                        n.put("mostrarNombre", !Boolean.FALSE.equals(b.get("nombre")));
                        n.put("mayusculas", !Boolean.FALSE.equals(b.get("mayusculas")));
                    }
                    case "partes" -> {
                        textos.put("proveedor", b.get("tituloProveedor"));
                        textos.put("cliente", b.get("tituloCliente"));
                    }
                    case "detalle" -> {
                        textos.put("detalle", b.get("titulo"));
                        n.put("numeroPartida", !Boolean.FALSE.equals(b.get("numero")));
                    }
                    case "condiciones" -> textos.put("condiciones", b.get("titulo"));
                    case "firma" -> n.put("firma", seccion(b.get("nombre"), b.get("puesto")));
                    case "texto" -> n.putIfAbsent("nota", seccion("", b.get("texto")));
                    default -> { }
                }
            }
        }
        textos.values().removeIf(Objects::isNull);
        return n;
    }

    private static Map<String, Object> seccion(Object titulo, Object texto) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("activo", true);
        s.put("titulo", titulo == null ? "" : titulo);
        s.put("texto", texto == null ? "" : texto);
        return s;
    }

    private String recurso(String ruta) {
        try (InputStream in = getClass().getResourceAsStream(ruta)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> leer(String v) {
        return json.readValue(v, Map.class);
    }
}
