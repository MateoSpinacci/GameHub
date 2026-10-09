package org.example.msgames.connector;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.msgames.interfaces.GamesConnector;
import org.example.msgames.model.Games;
import org.example.msgames.model.GamesSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    public List<Games> listar(String nombre) {
        GamesSearchResponse resultados = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("key", key)
                        .queryParam("search", nombre)
                        .build())
                .retrieve()
                .body(GamesSearchResponse.class);
        assert resultados != null;
        return resultados.getResults();
    }

    @Override
    public Games juegoPorId(Integer id) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/{id}")
                        .queryParam("key", key)
                        .build(id))
                .retrieve()
                .body(Games.class);
    }
}
