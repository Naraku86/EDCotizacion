package com.edcotizacion.web;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.edcotizacion.seguridad.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;

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
    public String cuenta(Principal usuario, Model model) {
        model.addAttribute("usuario", usuario.getName());
        return "cuenta";
    }

    @PostMapping("/cuenta")
    public String guardar(Principal usuario, @RequestParam String actual, @RequestParam String nombre,
            @RequestParam String nuevo, @RequestParam String confirmar,
            HttpServletRequest request, RedirectAttributes ra) throws ServletException {
        try {
            if (!nuevo.equals(confirmar)) {
                throw new IllegalArgumentException("La contraseña nueva y su confirmación no coinciden.");
            }
            usuarios.cambiar(usuario.getName(), actual, nombre, nuevo);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta";
        }
        // la sesión quedó con el nombre anterior: se vuelve a entrar con los datos nuevos
        request.logout();
        return "redirect:/login?cambio";
    }
}
