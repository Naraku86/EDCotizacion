package com.edcotizacion.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.edcotizacion.escritorio.Apagado;

/** "Cerrar programa" desde la barra (modo escritorio). Pide sesión y token CSRF como todo POST. */
@Controller
@ConditionalOnProperty(name = "app.modo", havingValue = "escritorio", matchIfMissing = true)
public class EscritorioController {

    private final Apagado apagado;

    public EscritorioController(Apagado apagado) {
        this.apagado = apagado;
    }

    @PostMapping("/apagar")
    public String apagar() {
        apagado.apagar();
        return "apagado";
    }
}
