package com.edcotizacion.pdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Revisa imágenes incrustadas (data URI) sin decodificarlas: solo lee el encabezado para saber
 * el formato real y las medidas. Así una imagen pequeña en bytes pero enorme en píxeles se
 * rechaza antes de que la librería del PDF intente cargarla en memoria.
 */
final class Imagenes {

    private static final Set<String> FORMATOS = Set.of("png", "jpeg");

    private Imagenes() {
    }

    /** true si es PNG o JPG de verdad y no pasa de maximo × maximo píxeles. */
    static boolean medidasPermitidas(String dataUri, int maximo) {
        int coma = dataUri.indexOf(',');
        if (coma < 0) {
            return false;
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dataUri.substring(coma + 1));
        } catch (IllegalArgumentException e) {
            return false;
        }
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> lectores = in == null ? null : ImageIO.getImageReaders(in);
            if (lectores == null || !lectores.hasNext()) {
                return false;
            }
            ImageReader lector = lectores.next();
            try {
                if (!FORMATOS.contains(lector.getFormatName().toLowerCase(Locale.ROOT))) {
                    return false;
                }
                lector.setInput(in, true, true);
                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                return ancho > 0 && alto > 0 && ancho <= maximo && alto <= maximo;
            } finally {
                lector.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
