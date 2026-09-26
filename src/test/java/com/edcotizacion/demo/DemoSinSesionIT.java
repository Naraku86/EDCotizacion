package com.edcotizacion.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.edcotizacion.PruebaIntegracion;

import tools.jackson.databind.json.JsonMapper;

/**
 * Con el servidor real (no MockMvc, que crea su propia sesión y cambia el manejo de CSRF):
 * una visita anónima al demo no abre sesión y el token CSRF viaja en una cookie.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoSinSesionIT extends PruebaIntegracion {

    private static final Pattern TOKEN = Pattern.compile("name=\"_csrf\" content=\"([^\"]+)\"");

    @LocalServerPort int puerto;
    @Autowired JsonMapper json;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> vista(String cookie, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/demo/vista"))
                .header("Content-Type", "application/json").header("X-CSRF-TOKEN", token)
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(DemoIT.form())));
        if (cookie != null) {
            b.header("Cookie", cookie);
        }
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void laVisitaNoAbreSesionYElTokenVaEnUnaCookie() throws Exception {
        HttpResponse<String> pagina = http.send(HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + puerto + "/demo")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, pagina.statusCode());

        List<String> cookies = pagina.headers().allValues("Set-Cookie");
        assertEquals(1, cookies.size(), "solo la cookie del token, ninguna de sesión: " + cookies);
        String cookie = cookies.getFirst();
        assertTrue(cookie.startsWith("EDCOT_DEMO_CSRF="), cookie);
        assertTrue(cookie.contains("HttpOnly") && cookie.contains("Path=/demo") && cookie.contains("SameSite=Strict"), cookie);

        Matcher m = TOKEN.matcher(pagina.body());
        assertTrue(m.find());
        String token = m.group(1);
        String valor = cookie.substring(0, cookie.indexOf(';'));

        HttpResponse<String> conCookie = vista(valor, token);
        assertEquals(200, conCookie.statusCode());
        assertTrue(conCookie.headers().allValues("Set-Cookie").stream().noneMatch(c -> !c.startsWith("EDCOT_DEMO_CSRF=")),
                "tampoco al generar la vista previa se abre sesión");
        assertEquals(403, vista(null, token).statusCode(), "sin la cookie el token no sirve");
    }
}
