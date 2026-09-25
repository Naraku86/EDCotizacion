package com.edcotizacion;

import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class EdCotizacionApplication {

    private static final Logger log = LoggerFactory.getLogger(EdCotizacionApplication.class);

    @Value("${app.abrir-navegador:true}")
    private boolean abrirNavegador;

    @Value("${server.port:8090}")
    private int puerto;

    public static void main(String[] args) throws Exception {
        // La carpeta de datos debe existir antes de que SQLite abra la base
        String home = System.getProperty("app.home");
        if (home == null) {
            home = Path.of(System.getProperty("user.home"), "EDCotizacion").toString();
            System.setProperty("app.home", home);
        }
        Files.createDirectories(Path.of(home));
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(EdCotizacionApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void abrirNavegador() {
        String url = "http://localhost:" + puerto;
        log.info("EDCotizacion lista en {}", url);
        if (!abrirNavegador) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Exception e) {
            log.warn("No se pudo abrir el navegador: {}", e.getMessage());
        }
    }
}
