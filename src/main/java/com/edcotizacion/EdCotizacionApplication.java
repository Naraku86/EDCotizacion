package com.edcotizacion;

import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.time.Clock;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import com.edcotizacion.escritorio.Arranque;
import com.edcotizacion.escritorio.InstanciaUnica;
import com.edcotizacion.escritorio.Navegador;

/**
 * Dos modos (app.modo):
 * <ul>
 * <li>escritorio (predeterminado): programa instalado en una PC; abre el navegador, muestra un
 *     icono en la bandeja y, si ya estaba abierto, solo abre el navegador en esa copia.</li>
 * <li>servidor: contenedor o servidor; sin navegador, sin bandeja y sin "Cerrar programa".</li>
 * </ul>
 */
@SpringBootApplication
public class EdCotizacionApplication {

    private static final Logger log = LoggerFactory.getLogger(EdCotizacionApplication.class);

    @Value("${app.abrir-navegador:true}")
    private boolean abrirNavegador;

    @Value("${app.modo:escritorio}")
    private String modo;

    @Value("${server.port:8090}")
    private int puerto;

    public static void main(String[] args) throws Exception {
        Arranque arranque = Arranque.de(args);
        if (arranque.escritorio()) {
            if (hayPantalla()) {
                System.setProperty("java.awt.headless", "false"); // bandeja y ventana de error
            }
            if (InstanciaUnica.yaAbierta(arranque.puerto())) {
                log.info("EDCotizacion ya está abierta en {}; se abre el navegador", arranque.url());
                Navegador.abrir(arranque.url());
                return;
            }
        }
        // La carpeta de datos debe existir antes de que SQLite abra la base
        Files.createDirectories(arranque.home());
        System.setProperty("app.home", arranque.home().toString());
        if (System.getProperty("logging.file.name") == null) {
            System.setProperty("logging.file.name", arranque.home().resolve("edcotizacion.log").toString());
        }
        try {
            SpringApplication.run(EdCotizacionApplication.class, args);
        } catch (RuntimeException e) {
            if (arranque.escritorio()) {
                avisarQueNoInicio(arranque, e);
            }
            throw e;
        }
    }

    /** En Linux sin sesión gráfica (p. ej. por SSH) AWT fallaría; en Windows y macOS siempre hay. */
    private static boolean hayPantalla() {
        if (!System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("linux")) {
            return true;
        }
        return System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null;
    }

    /** Instalada no hay consola: si no arranca (p. ej. puerto ocupado por otro programa), se avisa con una ventana. */
    private static void avisarQueNoInicio(Arranque arranque, RuntimeException e) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        Throwable causa = e;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        String detalle = causa instanceof java.net.BindException
                ? "El puerto " + arranque.puerto() + " está ocupado por otro programa."
                : causa.getMessage();
        try {
            JOptionPane.showMessageDialog(null,
                    "No se pudo iniciar EDCotizacion.\n" + detalle + "\n\nRegistro: "
                            + arranque.home().resolve("edcotizacion.log"),
                    "EDCotizacion", JOptionPane.ERROR_MESSAGE);
        } catch (Throwable sinVentana) {
            log.debug("No se pudo mostrar la ventana de error", sinVentana);
        }
    }

    /** Hora actual; como bean para poder fijarla en las pruebas. */
    @Bean
    Clock reloj() {
        return Clock.systemDefaultZone();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void abrirNavegador() {
        String url = "http://localhost:" + puerto;
        log.info("EDCotizacion lista en {}", url);
        if (abrirNavegador && Arranque.ESCRITORIO.equals(modo)) {
            Navegador.abrir(url);
        }
    }
}
