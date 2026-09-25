package com.edcotizacion.seguridad;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Login con formulario y cabeceras de seguridad. Todo pide sesión excepto la pantalla de
 * entrada y sus estilos. CSRF queda activo (los scripts mandan el token en cada POST/PUT).
 */
@Configuration
public class SeguridadConfig {

    /**
     * Solo scripts y recursos propios. 'unsafe-inline' en estilos porque el HTML del PDF lleva
     * su CSS en línea (y la vista previa del editor lo reutiliza); img data:/blob: para el logo.
     */
    static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data: blob:; font-src 'self'; connect-src 'self'; object-src 'none'; "
            + "base-uri 'self'; form-action 'self'; frame-ancestors 'none'";

    /** Los PDF se muestran con el visor del navegador; una CSP estricta en ellos puede bloquearlo. */
    private static final RequestMatcher NO_ES_PDF = r -> !r.getRequestURI().endsWith(".pdf")
            && !r.getRequestURI().endsWith("/pdf");

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        ExceptionMappingAuthenticationFailureHandler fallo = new ExceptionMappingAuthenticationFailureHandler();
        fallo.setExceptionMappings(Map.of(LockedException.class.getName(), "/login?bloqueado"));
        fallo.setDefaultFailureUrl("/login?error");

        http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/app.css", "/app.js", "/fuentes/**", "/favicon.ico", "/error").permitAll()
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", false).failureHandler(fallo).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?salio"))
            .headers(h -> h
                .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(NO_ES_PDF, new ContentSecurityPolicyHeaderWriter(CSP)))
                .referrerPolicy(r -> r.policy(ReferrerPolicy.SAME_ORIGIN)));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
