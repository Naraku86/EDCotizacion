package com.edcotizacion.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.edcotizacion.seguridad.LimiteDePeticion.PeticionDemasiadoGrandeException;

import jakarta.servlet.http.HttpServletRequest;

class LimiteDePeticionTest {

    private final LimiteDePeticion filtro = new LimiteDePeticion();

    /** Envío por partes (chunked): el servidor no conoce el largo antes de leer. */
    private static MockHttpServletRequest sinLargo(int bytes) {
        MockHttpServletRequest r = new MockHttpServletRequest("POST", "/demo/vista") {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        r.setContent(new byte[bytes]);
        return r;
    }

    /** Cadena que lee todo el cuerpo, como lo haría Jackson. */
    private static MockFilterChain leeTodo() {
        return new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException {
                ((HttpServletRequest) req).getInputStream().readAllBytes();
            }
        };
    }

    @Test
    void conLargoDeclaradoMayorSeRechazaSinLeer() throws Exception {
        MockHttpServletRequest r = new MockHttpServletRequest("POST", "/demo/vista");
        r.setContent(new byte[(int) LimiteDePeticion.MAXIMO + 1]);
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(r, res, cadena);
        assertEquals(413, res.getStatus());
        assertNull(cadena.getRequest(), "la petición no llega a la aplicación");
    }

    @Test
    void sinLargoSeCortaAlPasarElMaximoMientrasSeLee() {
        assertThrows(PeticionDemasiadoGrandeException.class, () -> filtro.doFilter(
                sinLargo((int) LimiteDePeticion.MAXIMO + 1), new MockHttpServletResponse(), leeTodo()));
    }

    @Test
    void sinLargoPeroDentroDelMaximoPasa() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro.doFilter(sinLargo(1024), res, leeTodo());
        assertEquals(200, res.getStatus());
    }
}
