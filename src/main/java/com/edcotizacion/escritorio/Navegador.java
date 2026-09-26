package com.edcotizacion.escritorio;

import java.awt.Desktop;
import java.net.URI;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Abre una dirección en el navegador predeterminado del sistema. */
public final class Navegador {

    private static final Logger log = LoggerFactory.getLogger(Navegador.class);

    private Navegador() {
    }

    public static void abrir(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            } else if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux")) {
                new ProcessBuilder("xdg-open", url).start();
            } else {
                log.info("Abre {} en tu navegador", url);
            }
        } catch (Exception e) {
            log.warn("No se pudo abrir el navegador: {}. Abre {} a mano.", e.getMessage(), url);
        }
    }
}
