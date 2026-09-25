package com.edcotizacion.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import com.edcotizacion.cotizacion.Cotizacion;
import com.edcotizacion.cotizacion.CotizacionService;
import com.edcotizacion.producto.Producto;
import com.edcotizacion.producto.ProductoRepository;

/** Autocompletado y guardado de cotizaciones desde el formulario. */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final CotizacionService service;

    public ApiController(ClienteRepository clientes, ProductoRepository productos, CotizacionService service) {
        this.clientes = clientes;
        this.productos = productos;
        this.service = service;
    }

    @GetMapping("/clientes")
    public List<Cliente> clientes(@RequestParam String q) {
        return clientes.buscar(q, 10);
    }

    @GetMapping("/productos")
    public List<Producto> productos(@RequestParam String q) {
        return productos.buscar(q, 10);
    }

    @PostMapping("/cotizaciones")
    public Map<String, Long> crear(@RequestBody Cotizacion c) {
        c.setId(null);
        return Map.of("id", service.guardar(c));
    }

    @PutMapping("/cotizaciones/{id}")
    public Map<String, Long> actualizar(@PathVariable long id, @RequestBody Cotizacion c) {
        c.setId(id);
        return Map.of("id", service.guardar(c));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> error(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
