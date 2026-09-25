package com.edcotizacion.producto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edcotizacion.comun.Busqueda;

@Service
public class ProductoService {

    private final ProductoRepository productos;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    /** Busca cada palabra por separado, sin acentos: "benq proy" encuentra "Proyector Benq ...". */
    @Transactional(readOnly = true)
    public List<ProductoSugerencia> buscar(String texto, int limite) {
        Busqueda b = new Busqueda(texto);
        return productos.findAllByOrderByDescripcion().stream()
                .filter(p -> b.coincide(p.getDescripcion()))
                .limit(limite)
                .map(ProductoSugerencia::de)
                .toList();
    }

    /** Da de alta el producto si no existe y recuerda el último costo y precio. Devuelve el id. */
    @Transactional
    public long registrar(String descripcion, BigDecimal costo, BigDecimal precio) {
        Producto p = productos.findByDescripcion(descripcion)
                .orElseGet(() -> productos.save(new Producto(descripcion)));
        p.recordarPrecios(costo, precio);
        return p.getId();
    }
}
