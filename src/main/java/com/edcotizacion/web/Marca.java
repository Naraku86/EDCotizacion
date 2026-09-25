package com.edcotizacion.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.edcotizacion.pdf.Empresa;
import com.edcotizacion.seguridad.UsuarioService;

/** Datos comunes de todas las pantallas: nombre de la aplicación en la barra y aviso de contraseña. */
@ControllerAdvice(assignableTypes = { CotizacionController.class, ConfigController.class,
        PlantillaController.class, CuentaController.class })
public class Marca {

    private final UsuarioService usuarios;
    private final boolean demoActivo;

    public Marca(UsuarioService usuarios, @Value("${app.demo.activo:true}") boolean demoActivo) {
        this.usuarios = usuarios;
        this.demoActivo = demoActivo;
    }

    @ModelAttribute("marca")
    public Empresa marca() {
        return new Empresa("ED Cotizaciones", null, null, null, null, null, null, null, null);
    }

    /** Para el enlace "Probar el demo" en la pantalla de entrada. */
    @ModelAttribute("demoActivo")
    public boolean demoActivo() {
        return demoActivo;
    }

    @ModelAttribute("passwordDefault")
    public boolean passwordDefault() {
        return usuarios.hayPasswordDefault();
    }
}
