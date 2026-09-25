package com.edcotizacion.seguridad;

import static com.edcotizacion.comun.Textos.limpio;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Formulario de Cuenta. La contraseña no se limpia (los espacios cuentan). Máximo 72 caracteres
 * porque BCrypt ignora lo que sigue.
 */
public record CambioCuenta(
        @NotBlank(message = "Escribe tu contraseña actual.") String actual,
        @NotBlank(message = "El usuario no puede quedar vacío.")
        @Size(max = 50, message = "El usuario es demasiado largo.")
        @Pattern(regexp = "[\\p{L}\\p{N}._@-]+", message = "El usuario solo puede tener letras, números y . _ @ -")
        String nombre,
        @NotNull(message = "Escribe la contraseña nueva.")
        @Size(min = 8, max = 72, message = "La contraseña nueva debe tener entre 8 y 72 caracteres.") String nuevo,
        String confirmar) {

    public CambioCuenta {
        nombre = limpio(nombre);
    }

    @AssertTrue(message = "La contraseña nueva y su confirmación no coinciden.")
    public boolean isConfirmada() {
        return nuevo != null && nuevo.equals(confirmar);
    }
}
