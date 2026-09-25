package com.edcotizacion.web;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.edcotizacion.config.ConfigService;
import com.edcotizacion.empresa.EmisorService;

@Controller
public class ConfigController {

    private static final List<String> CLAVES = List.of(
            ConfigService.FOLIO_PREFIJO, ConfigService.FOLIO_SIGUIENTE, ConfigService.IVA_TASA,
            ConfigService.GANANCIA_DEFAULT, ConfigService.VIGENCIA_DEFAULT, ConfigService.FORMA_PAGO,
            ConfigService.TIEMPO_ENTREGA, ConfigService.GARANTIA, ConfigService.OBSERVACIONES);

    private final ConfigService config;
    private final EmisorService emisores;

    public ConfigController(ConfigService config, EmisorService emisores) {
        this.config = config;
        this.emisores = emisores;
    }

    @GetMapping("/configuracion")
    public String ver(Model model) {
        model.addAttribute("empresas", emisores.todas());
        model.addAttribute("cfg", config.todos());
        model.addAttribute("folioSiguiente", config.get(ConfigService.FOLIO_PREFIJO)
                + String.format("%04d", config.getInt(ConfigService.FOLIO_SIGUIENTE)));
        return "configuracion";
    }

    @PostMapping("/configuracion/empresas")
    public String crearEmpresa(@RequestParam String nombre, RedirectAttributes ra) {
        try {
            long id = emisores.crear(nombre);
            return "redirect:/configuracion/empresas/" + id + "/plantilla";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/configuracion";
        }
    }

    @PostMapping("/configuracion")
    public String guardar(@RequestParam Map<String, String> valores, RedirectAttributes ra) {
        for (String clave : CLAVES) {
            if (valores.containsKey(clave)) {
                config.set(clave, valores.get(clave).trim());
            }
        }
        ra.addFlashAttribute("mensaje", "Configuración guardada.");
        return "redirect:/configuracion";
    }
}
