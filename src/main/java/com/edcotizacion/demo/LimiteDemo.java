package com.edcotizacion.demo;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Freno del demo público: cuántas vistas previas y PDF puede pedir cada IP por minuto, y
 * cuántos PDF se generan a la vez (para que el demo no deje sin CPU al resto de la app).
 * Los contadores viven en memoria; al reiniciar la app empiezan de cero.
 */
@Component
public class LimiteDemo {

    /** Se pasó del límite o el servidor está ocupado; el mensaje es para el usuario. */
    public static class LimiteExcedidoException extends RuntimeException {
        public LimiteExcedidoException(String mensaje) {
            super(mensaje);
        }
    }

    @FunctionalInterface
    public interface Tarea<T> {
        T hacer() throws Exception;
    }

    private static final Duration VENTANA = Duration.ofMinutes(1);
    /** Por encima de esto se borran las ventanas vencidas para que el mapa no crezca sin fin. */
    private static final int LIMPIAR_DESDE = 10_000;
    private static final long ESPERA_MAXIMA_SEGUNDOS = 10;

    private record Ventana(long inicio, int cuenta) {
    }

    private final int vistasPorMinuto;
    private final int pdfPorMinuto;
    private final Semaphore generando;
    private final Clock reloj;
    private final Map<String, Ventana> ventanas = new ConcurrentHashMap<>();

    @Autowired
    public LimiteDemo(@Value("${app.demo.vistas-por-minuto:90}") int vistasPorMinuto,
            @Value("${app.demo.pdf-por-minuto:10}") int pdfPorMinuto,
            @Value("${app.demo.pdf-simultaneos:2}") int pdfSimultaneos) {
        this(vistasPorMinuto, pdfPorMinuto, pdfSimultaneos, Clock.systemUTC());
    }

    LimiteDemo(int vistasPorMinuto, int pdfPorMinuto, int pdfSimultaneos, Clock reloj) {
        this.vistasPorMinuto = vistasPorMinuto;
        this.pdfPorMinuto = pdfPorMinuto;
        this.generando = new Semaphore(Math.max(1, pdfSimultaneos), true);
        this.reloj = reloj;
    }

    public void vista(String ip) {
        contar("vista|" + ip, vistasPorMinuto,
                "Demasiadas actualizaciones de la vista previa. Espera un minuto.");
    }

    public void pdf(String ip) {
        contar("pdf|" + ip, pdfPorMinuto, "Generaste demasiados PDF seguidos. Espera un minuto y vuelve a intentar.");
    }

    /** Ejecuta la tarea solo si hay lugar entre los PDF que se están generando. */
    public <T> T generar(Tarea<T> tarea) throws Exception {
        if (!generando.tryAcquire(ESPERA_MAXIMA_SEGUNDOS, TimeUnit.SECONDS)) {
            throw new LimiteExcedidoException("El demo está ocupado. Intenta de nuevo en unos segundos.");
        }
        try {
            return tarea.hacer();
        } finally {
            generando.release();
        }
    }

    private void contar(String clave, int limite, String mensaje) {
        long ahora = reloj.millis();
        if (ventanas.size() > LIMPIAR_DESDE) {
            ventanas.values().removeIf(v -> vencida(v, ahora));
        }
        Ventana v = ventanas.compute(clave, (k, actual) -> actual == null || vencida(actual, ahora)
                ? new Ventana(ahora, 1)
                : new Ventana(actual.inicio(), actual.cuenta() + 1));
        if (v.cuenta() > limite) {
            throw new LimiteExcedidoException(mensaje);
        }
    }

    private static boolean vencida(Ventana v, long ahora) {
        return ahora - v.inicio() >= VENTANA.toMillis();
    }
}
