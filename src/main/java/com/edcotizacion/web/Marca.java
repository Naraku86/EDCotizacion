package com.edcotizacion.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.edcotizacion.escritorio.Arranque;
import com.edcotizacion.pdf.Empresa;
import com.edcotizacion.prueba.InstanciaPrueba;
import com.edcotizacion.seguridad.UsuarioService;

/** Datos comunes de todas las pantallas: nombre de la aplicación en la barra y aviso de contraseña. */
@ControllerAdvice(assignableTypes = { CotizacionController.class, ConfigController.class,
        PlantillaController.class, CuentaController.class })
public class Marca {

    private final UsuarioService usuarios;
    private final boolean demoActivo;
    private final boolean escritorio;
    private final InstanciaPrueba prueba;

    public Marca(UsuarioService usuarios, @Value("${app.demo.activo:true}") boolean demoActivo,
            @Value("${app.modo:escritorio}") String modo, InstanciaPrueba prueba) {
        this.usuarios = usuarios;
        this.demoActivo = demoActivo;
        this.escritorio = Arranque.ESCRITORIO.equals(modo);
        this.prueba = prueba;
    }

    /** Instancia de prueba: aviso fijo, credenciales en la entrada y cuenta de solo lectura. */
    @ModelAttribute("prueba")
    public InstanciaPrueba prueba() {
        return prueba;
    }

    /** En modo escritorio la barra muestra "Cerrar programa". */
    @ModelAttribute("escritorio")
    public boolean escritorio() {
        return escritorio;
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
        // en la instancia de prueba la contraseña de fábrica es la de todos: no se pide cambiarla
        return !prueba.activa() && usuarios.hayPasswordDefault();
    }
}
