package com.edcotizacion.seguridad;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Quien entra con la contraseña de fábrica (admin/admin) debe cambiarla antes de usar la app.
 * Al entrar se marca la sesión y se lleva a Cuenta; mientras la marca exista, cualquier otra
 * pantalla redirige a Cuenta y cualquier otra acción responde 403. Cambiar la contraseña cierra
 * la sesión, y con ella la marca.
 */
public class CambioObligatorio implements HandlerInterceptor {

    static final String ATRIBUTO = CambioObligatorio.class.getName();
    static final String CUENTA = "/cuenta";

    /** true si esta sesión todavía debe cambiar la contraseña. */
    public static boolean pendiente(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null && Boolean.TRUE.equals(sesion.getAttribute(ATRIBUTO));
    }

    /** Al entrar: marca la sesión si usa la contraseña de fábrica y decide a dónde ir. */
    static AuthenticationSuccessHandler alEntrar(UsuarioService usuarios) {
        SavedRequestAwareAuthenticationSuccessHandler normal = new SavedRequestAwareAuthenticationSuccessHandler();
        normal.setDefaultTargetUrl("/");
        return (HttpServletRequest request, HttpServletResponse response, Authentication auth) -> {
            if (usuarios.usaPasswordDefault(auth.getName())) {
                request.getSession().setAttribute(ATRIBUTO, Boolean.TRUE);
                response.sendRedirect(request.getContextPath() + CUENTA);
                return;
            }
            try {
                normal.onAuthenticationSuccess(request, response, auth);
            } catch (ServletException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!pendiente(request)) {
            return true;
        }
        if ("GET".equals(request.getMethod())) {
            response.sendRedirect(request.getContextPath() + CUENTA);
        } else {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Cambia la contraseña de fábrica para continuar");
        }
        return false;
    }
}
