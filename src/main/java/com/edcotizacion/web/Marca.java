package com.edcotizacion.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.edcotizacion.pdf.DisenoService;
import com.edcotizacion.pdf.Empresa;
import com.edcotizacion.seguridad.UsuarioService;

/** Datos comunes de todas las pantallas: nombre de la empresa en la barra y aviso de contraseña. */
@ControllerAdvice(assignableTypes = { CotizacionController.class, ConfigController.class,
        PlantillaController.class, CuentaController.class })
public class Marca {

    private final DisenoService disenos;
    private final UsuarioService usuarios;

    public Marca(DisenoService disenos, UsuarioService usuarios) {
        this.disenos = disenos;
        this.usuarios = usuarios;
    }

    @ModelAttribute("marca")
    public Empresa marca() {
        return disenos.empresa();
    }

    @ModelAttribute("passwordDefault")
    public boolean passwordDefault() {
        return usuarios.hayPasswordDefault();
    }
}
