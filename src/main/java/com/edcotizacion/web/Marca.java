package com.edcotizacion.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.edcotizacion.pdf.DisenoService;
import com.edcotizacion.pdf.Empresa;

/** Nombre de la empresa en la barra superior de todas las pantallas. */
@ControllerAdvice(assignableTypes = { CotizacionController.class, ConfigController.class, PlantillaController.class })
public class Marca {

    private final DisenoService disenos;

    public Marca(DisenoService disenos) {
        this.disenos = disenos;
    }

    @ModelAttribute("marca")
    public Empresa marca() {
        return disenos.empresa();
    }
}
