package com.edcotizacion.seguridad;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Piezas de seguridad que viven en Spring MVC (no en la cadena de filtros). */
@Configuration
public class SeguridadWeb implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CambioObligatorio())
                .excludePathPatterns(CambioObligatorio.CUENTA, "/login", "/logout", "/error", "/apagar",
                        "/demo", "/demo/**", "/*.css", "/*.js", "/fuentes/**", "/favicon.ico");
    }
}
