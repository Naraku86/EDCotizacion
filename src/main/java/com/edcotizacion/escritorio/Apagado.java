package com.edcotizacion.escritorio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/** Cierra el programa (desde la bandeja o desde "Cerrar programa" en la barra). */
@Component
public class Apagado {

    private static final Logger log = LoggerFactory.getLogger(Apagado.class);

    private final ConfigurableApplicationContext contexto;

    public Apagado(ConfigurableApplicationContext contexto) {
        this.contexto = contexto;
    }

    /**
     * Se apaga en otro hilo y con una pausa corta, para que la petición que lo pidió alcance a
     * responder (la página "El programa se cerró").
     */
    public void apagar() {
        Thread hilo = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Cerrando EDCotizacion a petición del usuario");
            System.exit(SpringApplication.exit(contexto, () -> 0));
        }, "apagado");
        hilo.setDaemon(false);
        hilo.start();
    }
}
