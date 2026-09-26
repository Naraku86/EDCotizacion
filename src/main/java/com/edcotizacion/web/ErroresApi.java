package com.edcotizacion.web;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.edcotizacion.comun.NoEncontradoException;
import com.edcotizacion.seguridad.LimiteDePeticion.PeticionDemasiadoGrandeException;

/**
 * Errores de las peticiones JSON en un solo formato: {"error": "mensaje para el usuario"}.
 * Nunca se devuelven trazas ni detalles internos.
 */
@RestControllerAdvice(assignableTypes = { ApiController.class, PlantillaController.class, DemoController.class })
public class ErroresApi {

    private static final Logger log = LoggerFactory.getLogger(ErroresApi.class);
    private static final Pattern PARTIDA = Pattern.compile("^partidas\\[(\\d+)]");

    public record Error(String error) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> invalido(MethodArgumentNotValidException e) {
        Set<String> mensajes = new LinkedHashSet<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            Matcher m = PARTIDA.matcher(fe.getField());
            mensajes.add(m.find() ? "Partida " + (Integer.parseInt(m.group(1)) + 1) + ": " + fe.getDefaultMessage()
                    : fe.getDefaultMessage());
        }
        e.getBindingResult().getGlobalErrors().forEach(g -> mensajes.add(g.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, String.join("\n", mensajes));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Error> parametroInvalido(HandlerMethodValidationException e) {
        return error(HttpStatus.BAD_REQUEST, "Datos no válidos.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Error> ilegible(HttpMessageNotReadableException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof PeticionDemasiadoGrandeException grande) {
                return error(HttpStatus.CONTENT_TOO_LARGE, grande.getMessage());
            }
        }
        log.debug("JSON no válido", e);
        return error(HttpStatus.BAD_REQUEST, "Los datos enviados no tienen el formato esperado.");
    }

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<Error> noEncontrado(NoEncontradoException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Error> reglaDeNegocio(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private static ResponseEntity<Error> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new Error(mensaje));
    }
}
