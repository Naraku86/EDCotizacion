package com.edcotizacion.empresa;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edcotizacion.comun.NoEncontradoException;

import tools.jackson.databind.json.JsonMapper;

/** Alta y consulta de las empresas emisoras. Sus datos y diseño los edita DisenoService. */
@Service
public class EmisorService {

    private final EmisorRepository emisores;
    private final JsonMapper json;

    public EmisorService(EmisorRepository emisores, JsonMapper json) {
        this.emisores = emisores;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public List<Emisor> todas() {
        return emisores.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Emisor obtener(long id) {
        return emisores.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la empresa " + id));
    }

    /** La que se propone en cotizaciones nuevas y en las rutas sin empresa. */
    @Transactional(readOnly = true)
    public Emisor predeterminada() {
        return emisores.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoEncontradoException("No hay ninguna empresa registrada"));
    }

    /** Da de alta una empresa con el diseño genérico y su nombre en el pie. Devuelve el id. */
    @Transactional
    public long crear(String nombre) {
        String n = Emisor.nombreValido(nombre);
        Emisor e = new Emisor(n,
                json.writeValueAsString(Map.of("nombre", n)),
                json.writeValueAsString(Map.of("textos", Map.of("pie", n))));
        return emisores.save(e).getId();
    }
}
