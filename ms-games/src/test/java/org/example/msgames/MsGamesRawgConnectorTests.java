package org.example.msgames;

import org.example.msgames.connector.RAWGConnector;
import org.example.msgames.model.Games;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class MsGamesRawgConnectorTests {

    private MockRestServiceServer server;
    private RAWGConnector connector;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.rawg.io/api");

        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        connector = new RAWGConnector(restClient, "TestsKey");
    }

    @Test
    public void shouldReturnGamesWhenSearchSucceeds() {
        String json = """
                {
                  "count": 1,
                  "results": [
                    {
                      "id": 22509,
                      "name": "Minecraft"
                    }
                  ]
                }
                """;

        server.expect(requestTo("https://api.rawg.io/api/games?key=TestsKey&search=Minecraft"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<Games> games = connector.list("Minecraft");

        assertEquals(1, games.size());
        assertEquals(22509, games.get(0).getId());
        server.verify();
    }

    @Test
    public void shouldReturnEmptyListWhenNoGamesFound() {
        String json = """
                {
                  "count": 0,
                  "results": []
                }
                """;

        server.expect(requestTo("https://api.rawg.io/api/games?key=TestsKey&search=NonExistentGame"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<Games> games = connector.list("NonExistentGame");

        assertTrue(games.isEmpty());
        server.verify();
    }

    @Test
    public void shouldThrowBadGatewayWhenSearchBodyIsEmpty() {
        server.expect(requestTo("https://api.rawg.io/api/games?key=TestsKey&search=Minecraft"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withNoContent());

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.list("Minecraft"));
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }

    @Test
    public void shouldThrowBadGatewayWhenRawgReturnsServerError() {
        server.expect(requestTo("https://api.rawg.io/api/games?key=TestsKey&search=Minecraft"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.list("Minecraft"));
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }

    @Test
    public void shouldReturnGameWhenIdExists() {
        String json = """
                {
                  "id": 22509,
                  "name": "Minecraft",
                  "metacritic": 83,
                  "released": "2009-05-10",
                  "background_image": "https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg",
                  "rating": 4.44
                }
                """;

        server.expect(requestTo("https://api.rawg.io/api/games/22509?key=TestsKey"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Games game = connector.gameById(22509);

        assertEquals(22509, game.getId());
        assertEquals("Minecraft", game.getName());
        assertEquals(83, game.getMetacritic());
        assertEquals("2009-05-10", game.getReleased());
        assertEquals(4.44, game.getRating());
        assertEquals("https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg", game.getBackgroundImage());
        server.verify();
    }

    @Test
    public void shouldThrowNotFoundWhenGameDoesNotExist() {
        server.expect(requestTo("https://api.rawg.io/api/games/999999?key=TestsKey"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.gameById(999999));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        server.verify();
    }

    @Test
    public void shouldThrowBadGatewayWhenGameBodyIsEmpty() {
        server.expect(requestTo("https://api.rawg.io/api/games/22509?key=TestsKey"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withNoContent());

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.gameById(22509));
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }
}
