package com.edcotizacion.escritorio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Si el usuario abre el programa otra vez mientras ya está corriendo, no se levanta una segunda
 * copia (fallaría por el puerto ocupado): basta con abrir el navegador en la que ya existe.
 */
public final class InstanciaUnica {

    /** Texto que solo tiene la pantalla de entrada de esta app (título de la página). */
    static final String SENAL = "· Cotizaciones</title>";

    private InstanciaUnica() {
    }

    /** true si en ese puerto de esta computadora ya responde EDCotizacion. */
    public static boolean yaAbierta(int puerto) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        HttpRequest r = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/login"))
                .timeout(Duration.ofSeconds(2)).build();
        try {
            HttpResponse<String> res = http.send(r, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200 && res.body().contains(SENAL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false; // nada escucha ahí (o no es esta app)
        }
    }
}
