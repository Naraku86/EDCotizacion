package com.edcotizacion.seguridad;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rechaza peticiones enormes antes de leerlas (la app se puede abrir desde la red local).
 * Lo más grande que se manda es la plantilla con el logo (~1.5 MB).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LimiteDePeticion extends OncePerRequestFilter {

    static final long MAXIMO = 5L * 1024 * 1024;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getContentLengthLong() > MAXIMO) {
            response.sendError(HttpStatus.CONTENT_TOO_LARGE.value(), "Petición demasiado grande");
            return;
        }
        chain.doFilter(request, response);
    }
}
