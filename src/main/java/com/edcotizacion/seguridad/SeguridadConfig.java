package com.edcotizacion.seguridad;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Login con formulario y cabeceras de seguridad. Todo pide sesión excepto la pantalla de
 * entrada, sus estilos y el demo público (si app.demo.activo no es false), que tiene su propia
 * cadena sin sesión. CSRF queda activo (los scripts mandan el token en cada POST/PUT).
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

    /**
     * Demo público: sin login y sin sesión. El token CSRF va en una cookie (HttpOnly, solo para
     * /demo) en lugar de la sesión, para que cada visita anónima no deje una sesión abierta en memoria.
     * Si el demo está apagado esta cadena no existe y /demo cae en la principal (pide sesión).
     */
    @Bean
    @Order(1)
    @ConditionalOnProperty(name = "app.demo.activo", havingValue = "true", matchIfMissing = true)
    SecurityFilterChain demo(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository tokens = new CookieCsrfTokenRepository();
        tokens.setCookieName("EDCOT_DEMO_CSRF");
        tokens.setCookiePath("/demo");
        tokens.setHeaderName("X-CSRF-TOKEN"); // la misma cabecera que usa el resto de la app
        tokens.setCookieCustomizer(c -> c.httpOnly(true).sameSite("Strict"));
        http
            .securityMatcher("/demo", "/demo/**")
            .authorizeHttpRequests(a -> a.anyRequest().permitAll())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(c -> c.disable())
            .csrf(c -> c.csrfTokenRepository(tokens))
            // la cookie se escribe antes de empezar a mandar la página (luego ya no se podría)
            .addFilterAfter(new CargarTokenCsrf(), CsrfFilter.class)
            .headers(this::cabeceras);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain seguridad(HttpSecurity http, UsuarioService usuarios, IntentosLogin intentos,
            @Value("${app.demo.activo:true}") boolean demo) throws Exception {
        http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/app.css", "/app.js", "/fuentes/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers(demo ? new String[] { "/demo.js" } : new String[0]).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new FiltroIntentosLogin(intentos), UsernamePasswordAuthenticationFilter.class)
            .formLogin(f -> f.loginPage("/login")
                .successHandler(CambioObligatorio.alEntrar(usuarios))
                .failureUrl("/login?error")
                .permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?salio"))
            .headers(this::cabeceras);
        return http.build();
    }

    private void cabeceras(HeadersConfigurer<HttpSecurity> h) {
        h.addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(NO_ES_PDF, new ContentSecurityPolicyHeaderWriter(CSP)))
            .referrerPolicy(r -> r.policy(ReferrerPolicy.SAME_ORIGIN));
    }

    /** Pide el token CSRF al inicio de la petición para que CookieCsrfTokenRepository pueda escribir la cookie. */
    private static final class CargarTokenCsrf extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            if (request.getAttribute(CsrfToken.class.getName()) instanceof CsrfToken token) {
                token.getToken();
            }
            chain.doFilter(request, response);
        }
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
