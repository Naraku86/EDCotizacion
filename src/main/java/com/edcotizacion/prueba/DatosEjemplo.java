package com.edcotizacion.prueba;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.edcotizacion.cotizacion.CotizacionForm;
import com.edcotizacion.cotizacion.CotizacionForm.PartidaForm;
import com.edcotizacion.cotizacion.CotizacionService;
import com.edcotizacion.cotizacion.DatosCliente;
import com.edcotizacion.cotizacion.Estado;
import com.edcotizacion.empresa.EmisorService;
import com.edcotizacion.pdf.DisenoService;
import com.edcotizacion.pdf.Empresa;

/**
 * En la instancia de prueba, si la base está vacía (recién creada o recién borrada), carga dos
 * empresas y algunas cotizaciones ficticias para que quien la visita vea la app funcionando.
 * Usa los mismos servicios que las pantallas, así que los datos pasan por las mismas validaciones.
 */
@Component
public class DatosEjemplo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosEjemplo.class);

    private final InstanciaPrueba prueba;
    private final EmisorService emisores;
    private final DisenoService disenos;
    private final CotizacionService cotizaciones;

    public DatosEjemplo(InstanciaPrueba prueba, EmisorService emisores, DisenoService disenos,
            CotizacionService cotizaciones) {
        this.prueba = prueba;
        this.emisores = emisores;
        this.disenos = disenos;
        this.cotizaciones = cotizaciones;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!prueba.activa() || !prueba.datosEjemplo() || cotizaciones.hayCotizaciones()) {
            return;
        }
        long norte = emisores.predeterminada().getId();
        disenos.guardar(norte,
                Map.of("plantilla", "clasica",
                        "colores", Map.of("primario", "#184E62", "acento", "#1A9C8D"),
                        "textos", Map.of("pie", "Soluciones Norte  •  Equipo de cómputo  •  Soporte")),
                new Empresa("Soluciones Norte", "Equipo de cómputo y soporte técnico", "SNO200101AB1",
                        "55 1234 5678", "ventas@solucionesnorte.example", "solucionesnorte.example",
                        "Av. Reforma 100, Col. Centro, Ciudad de México", "Laura Méndez", null));

        long taller = emisores.crear("Taller Hernández");
        disenos.guardar(taller,
                Map.of("plantilla", "moderna",
                        "colores", Map.of("primario", "#2D3436", "acento", "#E17055"),
                        "textos", Map.of("pie", "Taller Hernández  •  Servicio automotriz")),
                new Empresa("Taller Hernández", "Servicio automotriz", null, "33 9876 5432", null, null,
                        "Calle Industria 45, Guadalajara, Jal.", "Jorge Hernández", null));

        long a = cotizacion(norte, "Escuela Primaria Benito Juárez", 40,
                partida("Laptop 14\" Core i5, 16 GB RAM, 512 GB SSD", "10", "9800", "12740"),
                partida("Proyector 3600 lúmenes", "2", "7200", "9360"));
        long b = cotizacion(norte, "Clínica Santa Fe", 12,
                partida("Impresora láser multifuncional", "3", "4100", "5330"),
                partida("Instalación y configuración en red", "1", "0", "1200"));
        cotizacion(norte, "Despacho Contable Ruiz", 1,
                partida("Servidor de archivos NAS 4 bahías", "1", "11200", "14560"),
                partida("Disco duro 4 TB", "4", "1900", "2470"));
        long d = cotizacion(taller, "Transportes del Bajío", 3,
                partida("Afinación mayor", "1", null, "2800"),
                partida("Cambio de balatas delanteras", "2", null, "950"));
        cotizaciones.cambiarEstado(a, Estado.ACEPTADA);
        cotizaciones.cambiarEstado(b, Estado.ENVIADA);
        cotizaciones.cambiarEstado(d, Estado.ENVIADA);
        log.info("Instancia de prueba: datos de ejemplo cargados");
    }

    private long cotizacion(long empresa, String cliente, int diasAtras, PartidaForm... partidas) {
        return cotizaciones.guardar(null, new CotizacionForm(empresa, LocalDate.now().minusDays(diasAtras), 15,
                new DatosCliente(cliente, null, null, null, null, null), true, new BigDecimal("16"),
                new BigDecimal("150"), "50% anticipo y 50% contra entrega.", "3 a 5 días hábiles.",
                "12 meses con el fabricante.", null, Arrays.asList(partidas)));
    }

    private static PartidaForm partida(String descripcion, String cantidad, String costo, String precio) {
        return new PartidaForm(descripcion, new BigDecimal(cantidad), costo == null ? null : new BigDecimal(costo),
                new BigDecimal(precio), null);
    }
}
