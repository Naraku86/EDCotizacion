package com.edcotizacion.web;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.edcotizacion.seguridad.CambioCuenta;
import com.edcotizacion.seguridad.CambioObligatorio;
import com.edcotizacion.seguridad.UsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** Pantalla de entrada y cambio de usuario/contraseña. */
@Controller
public class CuentaController {

    private final UsuarioService usuarios;

    public CuentaController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cuenta")
    public String cuenta(Principal usuario, HttpServletRequest request, Model model) {
        model.addAttribute("usuario", usuario.getName());
        model.addAttribute("obligatorio", CambioObligatorio.pendiente(request));
        return "cuenta";
    }

    @PostMapping("/cuenta")
    public String guardar(Principal usuario, @Valid CambioCuenta cambio, BindingResult errores,
            HttpServletRequest request, RedirectAttributes ra) throws ServletException {
        if (errores.hasErrors()) {
            ra.addFlashAttribute("error", errores.getAllErrors().getFirst().getDefaultMessage());
            return "redirect:/cuenta";
        }
        try {
            usuarios.cambiar(usuario.getName(), cambio);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta";
        }
        // la sesión quedó con el nombre anterior: se vuelve a entrar con los datos nuevos
        request.logout();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return "redirect:/login?cambio";
    }
}
