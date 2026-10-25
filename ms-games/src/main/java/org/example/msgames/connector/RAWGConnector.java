package org.example.msgames.connector;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.msgames.interfaces.GamesConnector;
import org.example.msgames.model.Games;
import org.example.msgames.dto.GamesSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RAWGConnector implements GamesConnector {

    RestClient restClient;
    String key;

    public RAWGConnector(RestClient restClient, @Value("${rawg.api.key}") String key) {
        this.restClient = restClient;
        this.key = key;
    }

    @Override
    public List<Games> list(String nombre) {
        try {
            GamesSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games")
                            .queryParam("key", key)
                            .queryParam("search", nombre)
                            .build())
                    .retrieve()
                    .body(GamesSearchResponse.class);
            if (response == null || response.getResults() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid response from RAWG: missing results"
                );
            }

            return response.getResults();
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to communicate with RAWG",
                    e
            );
        } catch (HttpServerErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "RAWG returned a server error",
                    e
            );
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "RAWG returned an unexpected HTTP client error",
                    e
            );
        }
    }

    @Override
    public Games gameById(Integer id) {
        try {
            Games game = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games/{id}")
                            .queryParam("key", key)
                            .build(id))
                    .retrieve()
                    .body(Games.class);

            if (game == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid response from RAWG: missing game data"
                );
            }

            return game;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Game not found",
                    e
            );
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to communicate with RAWG",
                    e
            );
        } catch (HttpServerErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "RAWG returned a server error",
                    e
            );
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "RAWG returned an unexpected HTTP client error",
                    e
            );
        }
    }
}
