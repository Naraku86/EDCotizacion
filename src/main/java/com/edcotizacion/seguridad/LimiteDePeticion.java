package com.edcotizacion.seguridad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rechaza peticiones enormes (la app se puede abrir desde la red local y el demo es público).
 * Lo más grande que se manda es la plantilla con el logo (~1.5 MB).
 * Con Content-Length se rechaza antes de leer; sin él (envío por partes, "chunked") se cuenta
 * mientras se lee y se corta al pasar el máximo.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LimiteDePeticion extends OncePerRequestFilter {

    static final long MAXIMO = 5L * 1024 * 1024;

    /** El cuerpo pasó del máximo mientras se leía. */
    public static class PeticionDemasiadoGrandeException extends IOException {
        public PeticionDemasiadoGrandeException() {
            super("La petición es demasiado grande (máximo 5 MB).");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long largo = request.getContentLengthLong();
        if (largo > MAXIMO) {
            response.sendError(HttpStatus.CONTENT_TOO_LARGE.value(), "Petición demasiado grande");
            return;
        }
        chain.doFilter(largo < 0 ? new Limitada(request) : request, response);
    }

    /** Petición cuyo cuerpo no puede leerse más allá de MAXIMO bytes. */
    static final class Limitada extends HttpServletRequestWrapper {

        private ServletInputStream entrada;

        Limitada(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (entrada == null) {
                entrada = new Contada(super.getInputStream());
            }
            return entrada;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            String cs = getCharacterEncoding();
            Charset charset = cs == null ? StandardCharsets.UTF_8 : Charset.forName(cs);
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
    }

    private static final class Contada extends ServletInputStream {

        private final ServletInputStream original;
        private long leidos;

        Contada(ServletInputStream original) {
            this.original = original;
        }

        @Override
        public int read() throws IOException {
            int b = original.read();
            if (b >= 0) {
                sumar(1);
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = original.read(b, off, len);
            if (n > 0) {
                sumar(n);
            }
            return n;
        }

        private void sumar(int n) throws PeticionDemasiadoGrandeException {
            leidos += n;
            if (leidos > MAXIMO) {
                throw new PeticionDemasiadoGrandeException();
            }
        }

        @Override public boolean isFinished() { return original.isFinished(); }
        @Override public boolean isReady() { return original.isReady(); }
        @Override public void setReadListener(ReadListener listener) { original.setReadListener(listener); }
    }
}
