package com.edcotizacion.cotizacion;

import static com.edcotizacion.comun.Textos.limpio;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos del cliente tal como quedaron en la cotización (una copia: si después cambia el
 * catálogo de clientes, la cotización impresa no cambia). También es lo que manda el formulario.
 */
public record DatosCliente(
        @NotBlank(message = "Captura el nombre del cliente.")
        @Size(max = 200, message = "El nombre del cliente es demasiado largo.") String nombre,
        @Size(max = 200, message = "El contacto es demasiado largo.") String contacto,
        @Size(max = 50, message = "El teléfono es demasiado largo.") String telefono,
        @Email(message = "El correo del cliente no es válido.")
        @Size(max = 200, message = "El correo es demasiado largo.") String email,
        @Size(max = 20, message = "El RFC es demasiado largo.") String rfc,
        @Size(max = 500, message = "La dirección es demasiado larga.") String direccion) {

    public DatosCliente {
        nombre = limpio(nombre);
        contacto = limpio(contacto);
        telefono = limpio(telefono);
        email = limpio(email);
        rfc = limpio(rfc);
        direccion = limpio(direccion);
    }

    public static DatosCliente vacio() {
        return new DatosCliente(null, null, null, null, null, null);
    }
}
