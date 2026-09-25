package com.edcotizacion.web;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edcotizacion.cliente.Cliente;
import com.edcotizacion.cliente.ClienteRepository;
import com.edcotizacion.cotizacion.CotizacionForm;
import com.edcotizacion.cotizacion.CotizacionService;
import com.edcotizacion.cotizacion.DatosCliente;
import com.edcotizacion.producto.ProductoRepository;
import com.edcotizacion.producto.ProductoSugerencia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/** Autocompletado y guardado de cotizaciones desde el formulario. */
@RestController
@RequestMapping("/api")
@Validated
public class ApiController {

    private static final int SUGERENCIAS = 10;

    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final CotizacionService service;

    public ApiController(ClienteRepository clientes, ProductoRepository productos, CotizacionService service) {
        this.clientes = clientes;
        this.productos = productos;
        this.service = service;
    }

    @GetMapping("/clientes")
    public List<DatosCliente> clientes(@RequestParam @Size(max = 100) String q) {
        return clientes.buscar(q, SUGERENCIAS).stream().map(Cliente::datos).toList();
    }

    @GetMapping("/productos")
    public List<ProductoSugerencia> productos(@RequestParam @Size(max = 100) String q) {
        return productos.buscar(q, SUGERENCIAS).stream().map(ProductoSugerencia::de).toList();
    }

    @PostMapping("/cotizaciones")
    public Map<String, Long> crear(@Valid @RequestBody CotizacionForm form) {
        return Map.of("id", service.guardar(null, form));
    }

    @PutMapping("/cotizaciones/{id}")
    public Map<String, Long> actualizar(@PathVariable long id, @Valid @RequestBody CotizacionForm form) {
        return Map.of("id", service.guardar(id, form));
    }
}
