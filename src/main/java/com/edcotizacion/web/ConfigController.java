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

@Controller
public class ConfigController {

    private static final List<String> CLAVES = List.of(
            ConfigService.FOLIO_PREFIJO, ConfigService.FOLIO_SIGUIENTE, ConfigService.IVA_TASA,
            ConfigService.GANANCIA_DEFAULT, ConfigService.VIGENCIA_DEFAULT, ConfigService.FORMA_PAGO,
            ConfigService.TIEMPO_ENTREGA, ConfigService.GARANTIA, ConfigService.OBSERVACIONES);

    private final ConfigService config;

    public ConfigController(ConfigService config) {
        this.config = config;
    }

    @GetMapping("/configuracion")
    public String ver(Model model) {
        model.addAttribute("cfg", config.todos());
        model.addAttribute("folioSiguiente", config.get(ConfigService.FOLIO_PREFIJO)
                + String.format("%04d", config.getInt(ConfigService.FOLIO_SIGUIENTE)));
        return "configuracion";
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
