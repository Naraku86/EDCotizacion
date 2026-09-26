package com.edcotizacion.escritorio;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Lo que hay que saber antes de levantar Spring: modo, puerto y carpeta de datos. Cada valor
 * se toma, en este orden, de los argumentos (--app.modo=...), de las propiedades del sistema
 * (-Dapp.modo=...), de las variables de entorno (APP_MODO) y de &lt;carpeta de datos&gt;/application.yml,
 * igual que lo haría Spring.
 */
public record Arranque(boolean escritorio, int puerto, Path home) {

    public static final String ESCRITORIO = "escritorio";
    public static final String SERVIDOR = "servidor";

    public static Arranque de(String[] args) {
        Path home = Path.of(valor(args, "app.home", Path.of(System.getProperty("user.home"), "EDCotizacion").toString()));
        Map<String, Object> archivo = archivo(home.resolve("application.yml"));
        String modo = valor(args, "app.modo", porRuta(archivo, "app.modo", ESCRITORIO)).toLowerCase(Locale.ROOT);
        if (!modo.equals(ESCRITORIO) && !modo.equals(SERVIDOR)) {
            throw new IllegalArgumentException("app.modo debe ser 'escritorio' o 'servidor', no '" + modo + "'");
        }
        int puerto = Integer.parseInt(valor(args, "server.port", porRuta(archivo, "server.port", "8090")));
        return new Arranque(modo.equals(ESCRITORIO), puerto, home);
    }

    /** application.yml de la carpeta de datos; vacío si no existe o no se puede leer (Spring avisará). */
    @SuppressWarnings("unchecked")
    static Map<String, Object> archivo(Path ruta) {
        if (!Files.isRegularFile(ruta)) {
            return Map.of();
        }
        try (Reader r = Files.newBufferedReader(ruta)) {
            Object datos = new Yaml(new SafeConstructor(new LoaderOptions())).load(r);
            return datos instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        } catch (Exception e) {
            return Map.of();
        }
    }

    /** "server.port" -> archivo.server.port (anidado) o la clave plana "server.port". */
    static String porRuta(Map<String, Object> archivo, String clave, String predeterminado) {
        Object plano = archivo.get(clave);
        if (plano != null) {
            return plano.toString();
        }
        Object actual = archivo;
        for (String parte : clave.split("\\.")) {
            if (!(actual instanceof Map<?, ?> m)) {
                return predeterminado;
            }
            actual = m.get(parte);
        }
        return actual == null || actual instanceof Map ? predeterminado : actual.toString();
    }

    static String valor(String[] args, String clave, String predeterminado) {
        String prefijo = "--" + clave + "=";
        for (String a : args) {
            if (a.startsWith(prefijo)) {
                return a.substring(prefijo.length());
            }
        }
        String propiedad = System.getProperty(clave);
        if (propiedad != null) {
            return propiedad;
        }
        // regla de Spring: mayúsculas, "." -> "_" y sin guiones (app.abrir-navegador -> APP_ABRIRNAVEGADOR)
        String entorno = System.getenv(clave.toUpperCase(Locale.ROOT).replace('.', '_').replace("-", ""));
        return entorno != null && !entorno.isBlank() ? entorno : predeterminado;
    }

    public String url() {
        return "http://localhost:" + puerto;
    }
}
