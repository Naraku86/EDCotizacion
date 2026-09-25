package com.edcotizacion.cliente;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edcotizacion.comun.Busqueda;
import com.edcotizacion.cotizacion.DatosCliente;

@Service
public class ClienteService {

    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    /** Busca cada palabra sin acentos en nombre y contacto (el catálogo es chico: se filtra en memoria). */
    @Transactional(readOnly = true)
    public List<DatosCliente> buscar(String texto, int limite) {
        Busqueda b = new Busqueda(texto);
        return clientes.findAllByOrderByNombre().stream()
                .filter(c -> b.coincide(c.getNombre() + " " + Objects.toString(c.getContacto(), "")))
                .limit(limite)
                .map(Cliente::datos)
                .toList();
    }

    /** Da de alta el cliente si no existe; si existe completa sus datos. Devuelve el id. */
    @Transactional
    public long registrar(DatosCliente datos) {
        Cliente c = clientes.findByNombre(datos.nombre())
                .map(existente -> {
                    existente.actualizarCon(datos);
                    return existente;
                })
                .orElseGet(() -> clientes.save(new Cliente(datos)));
        return c.getId();
    }
}
