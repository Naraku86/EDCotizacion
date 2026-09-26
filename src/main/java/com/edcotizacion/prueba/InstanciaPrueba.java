package com.edcotizacion.prueba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Instancia de prueba pública (app.prueba.*): todos entran con admin / admin, la cuenta no se puede
 * cambiar y un aviso fijo recuerda que los datos son visibles para otros y se borran. El borrado
 * lo hace quien opera el servidor (por ejemplo, un temporizador que recrea el contenedor con un
 * volumen nuevo); la app solo carga datos de ejemplo cuando arranca con la base vacía.
 *
 * @param activa       true = instancia de prueba
 * @param datosEjemplo cargar una empresa y algunas cotizaciones de ejemplo si no hay ninguna
 * @param aviso        texto del aviso que aparece en todas las pantallas
 */
@ConfigurationProperties("app.prueba")
public record InstanciaPrueba(
        boolean activa,
        @DefaultValue("true") boolean datosEjemplo,
        @DefaultValue("Instancia de prueba: todos usan la misma cuenta, así que otras personas pueden ver lo que captures. Los datos se borran cada 8 horas.")
        String aviso) {
}
