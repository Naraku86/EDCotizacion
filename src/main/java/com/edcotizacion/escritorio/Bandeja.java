package com.edcotizacion.escritorio;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

/**
 * Icono junto al reloj con "Abrir" y "Salir": instalada, la app no tiene ventana y esta es la
 * forma de cerrarla. Solo en modo escritorio y si el sistema tiene bandeja (algunos escritorios
 * Linux no la tienen; para esos queda "Cerrar programa" en la barra de la app).
 */
@Component
public class Bandeja {

    private static final Logger log = LoggerFactory.getLogger(Bandeja.class);

    private final Apagado apagado;
    private final boolean escritorio;
    private final int puerto;
    private volatile TrayIcon icono;

    public Bandeja(Apagado apagado, @Value("${app.modo:escritorio}") String modo,
            @Value("${server.port:8090}") int puerto) {
        this.apagado = apagado;
        this.escritorio = Arranque.ESCRITORIO.equals(modo);
        this.puerto = puerto;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void mostrar() {
        try {
            if (!escritorio || GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
                return;
            }
        } catch (Throwable sinPantalla) { // AWTError si no hay sesión gráfica
            log.info("Sin bandeja del sistema: {}", sinPantalla.getMessage());
            return;
        }
        EventQueue.invokeLater(() -> {
            try {
                String url = "http://localhost:" + puerto;
                MenuItem abrir = new MenuItem("Abrir EDCotizacion");
                abrir.addActionListener(e -> Navegador.abrir(url));
                MenuItem salir = new MenuItem("Salir");
                salir.addActionListener(e -> apagado.apagar());
                PopupMenu menu = new PopupMenu();
                menu.add(abrir);
                menu.addSeparator();
                menu.add(salir);

                TrayIcon t = new TrayIcon(imagen(), "EDCotizacion · " + url, menu);
                t.setImageAutoSize(true);
                t.addActionListener(e -> Navegador.abrir(url)); // doble clic
                SystemTray.getSystemTray().add(t);
                icono = t;
            } catch (Exception | Error e) {
                log.warn("No se pudo mostrar el icono en la bandeja: {}", e.getMessage());
            }
        });
    }

    /** true si el icono está visible (la barra de la app lo menciona para cerrar el programa). */
    public boolean activa() {
        return icono != null;
    }

    @PreDestroy
    public void quitar() {
        TrayIcon t = icono;
        if (t != null) {
            EventQueue.invokeLater(() -> SystemTray.getSystemTray().remove(t));
        }
    }

    private static Image imagen() throws IOException {
        try (InputStream in = Bandeja.class.getResourceAsStream("/escritorio/icono.png")) {
            return ImageIO.read(in);
        }
    }
}
