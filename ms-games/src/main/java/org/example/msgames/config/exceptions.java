package org.example.msgames.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class exceptions {
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ProblemDetail handleNotFound(HttpClientErrorException.NotFound e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Juego no encontrado en RAWG");
    }

    @ExceptionHandler(RestClientException.class)
    public ProblemDetail handleRawgError(RestClientException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Error al comunicarse con RAWG");
    }
}
