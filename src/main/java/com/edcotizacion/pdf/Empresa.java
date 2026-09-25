package com.edcotizacion.pdf;

import static com.edcotizacion.comun.Textos.limpio;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos del proveedor que imprime el PDF. Se guardan con la plantilla de cada empresa emisora.
 * Los campos vacíos quedan en null para que la plantilla no imprima "RFC:" sin valor.
 *
 * @param logo imagen como data URI (data:image/png;base64,...)
 */
public record Empresa(
        @Size(max = 200, message = "El nombre de la empresa es demasiado largo.") String nombre,
        @Size(max = 300, message = "El lema es demasiado largo.") String lema,
        @Size(max = 20, message = "El RFC es demasiado largo.") String rfc,
        @Size(max = 50, message = "El teléfono es demasiado largo.") String telefono,
        @Email(message = "El correo de la empresa no es válido.")
        @Size(max = 200, message = "El correo es demasiado largo.") String correo,
        @Size(max = 200, message = "La página web es demasiado larga.") String web,
        @Size(max = 500, message = "La dirección es demasiado larga.") String direccion,
        @Size(max = 200, message = "El nombre del ejecutivo es demasiado largo.") String ejecutivo,
        @Size(max = Empresa.LOGO_MAXIMO, message = "El logo es demasiado grande (máximo 1.5 MB).")
        @Pattern(regexp = "data:image/(png|jpeg);base64,[A-Za-z0-9+/]+=*", message = "El logo debe ser una imagen PNG o JPG.")
        String logo) {

    /** ~1.5 MB de imagen en base64. */
    public static final int LOGO_MAXIMO = 2_000_000;

    public Empresa {
        nombre = limpio(nombre);
        lema = limpio(lema);
        rfc = limpio(rfc);
        telefono = limpio(telefono);
        correo = limpio(correo);
        web = limpio(web);
        direccion = limpio(direccion);
        ejecutivo = limpio(ejecutivo);
        logo = limpio(logo);
    }

    /** "EV Soluciones" -> 0: "EV", 1: "Soluciones". Para el nombre en dos colores. */
    public String nombreParte(int i) {
        String n = nombre == null ? "" : nombre;
        int esp = n.indexOf(' ');
        if (esp < 0) {
            return i == 0 ? n : "";
        }
        return i == 0 ? n.substring(0, esp) : n.substring(esp + 1).strip();
    }
}
