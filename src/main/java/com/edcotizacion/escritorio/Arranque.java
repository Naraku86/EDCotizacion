package com.edcotizacion.escritorio;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Lo que hay que saber antes de levantar Spring: modo, puerto y carpeta de datos. Cada valor
 * se toma, en este orden, de los argumentos (--app.modo=...), de las propiedades del sistema
 * (-Dapp.modo=...) y de las variables de entorno (APP_MODO), igual que lo haría Spring.
 */
public record Arranque(boolean escritorio, int puerto, Path home) {

    public static final String ESCRITORIO = "escritorio";
    public static final String SERVIDOR = "servidor";

    public static Arranque de(String[] args) {
        String modo = valor(args, "app.modo", ESCRITORIO).toLowerCase(Locale.ROOT);
        if (!modo.equals(ESCRITORIO) && !modo.equals(SERVIDOR)) {
            throw new IllegalArgumentException("app.modo debe ser 'escritorio' o 'servidor', no '" + modo + "'");
        }
        int puerto = Integer.parseInt(valor(args, "server.port", "8090"));
        String home = valor(args, "app.home", Path.of(System.getProperty("user.home"), "EDCotizacion").toString());
        return new Arranque(modo.equals(ESCRITORIO), puerto, Path.of(home));
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
        String entorno = System.getenv(clave.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_'));
        return entorno != null && !entorno.isBlank() ? entorno : predeterminado;
    }

    public String url() {
        return "http://localhost:" + puerto;
    }
}
