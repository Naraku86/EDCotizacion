package com.edcotizacion.seguridad;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Antes de revisar la contraseña: si ese usuario (o esa IP) está bloqueado, responde igual que
 * con una contraseña incorrecta y no intenta autenticar. No es @Component a propósito: solo se
 * registra dentro de la cadena de Spring Security (SeguridadConfig).
 */
class FiltroIntentosLogin extends OncePerRequestFilter {

    private final IntentosLogin intentos;

    FiltroIntentosLogin(IntentosLogin intentos) {
        this.intentos = intentos;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod())
                || !(request.getContextPath() + "/login").equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (intentos.bloqueado(request.getParameter("username"), request.getRemoteAddr())) {
            response.sendRedirect(request.getContextPath() + "/login?error");
            return;
        }
        chain.doFilter(request, response);
    }
}
