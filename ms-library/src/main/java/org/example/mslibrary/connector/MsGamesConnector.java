package org.example.mslibrary.connector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.mslibrary.dto.RAWGResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MsGamesConnector {

    RestClient restClient;

    public boolean existeJuegoPorId(Integer id) {
        try {
            restClient.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo establecer comunicación con ms-games",
                    e
            );
        }
    }

    public RAWGResponse getJuegoPorId(Integer id) {
        try {
            return restClient.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .body(RAWGResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se pudo encontrar el juego solicitado",
                    e
            );
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo establecer comunicación con ms-games",
                    e
            );
        }
    }
}
