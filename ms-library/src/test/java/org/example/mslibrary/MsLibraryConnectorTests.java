package org.example.mslibrary;

import org.example.mslibrary.connector.MsGamesConnector;
import org.example.mslibrary.dto.RAWGResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class MsLibraryConnectorTests {

    private MockRestServiceServer server;
    private MsGamesConnector connector;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081/api/games");

        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        connector = new MsGamesConnector(restClient);
    }

    @Test
    void shouldReturnFalseWhenGameDoesNotExist() throws Exception {
        server.expect(requestTo("http://localhost:8081/api/games/22509"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        boolean exists = connector.existsById(22509);

        assertFalse(exists);
        server.verify();
    }

    @Test
    void shouldReturnTrueWhenGameExists() {
        server.expect(requestTo("http://localhost:8081/api/games/22509"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK));

        boolean exists = connector.existsById(22509);

        assertTrue(exists);
        server.verify();
    }

    @Test
    void shouldThrowBadGatewayWhenExistsByIdReturns500() {
        server.expect(requestTo("http://localhost:8081/api/games/22509"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.existsById(22509));
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }

    @Test
    void shouldThrowBadGatewayWhenExistsByIdReturns403() {
        server.expect(requestTo("http://localhost:8081/api/games/22509"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.existsById(22509));
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }

    @Test
    void shouldReturnGameWhenGetGameByIdReturns200() {
        String json = """
        {
            "id": 22509,
            "name": "Minecraft",
            "released": "2009-05-10",
            "rating": 4.44,
            "metacritic": 83,
            "background_image": "https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg"
        }
        """;

        server.expect(requestTo("http://localhost:8081/api/games/22509"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        RAWGResponse response = connector.getGameById(22509);

        assertAll(
                () -> assertEquals(22509, response.getId()),
                () -> assertEquals("Minecraft", response.getName()),
                () -> assertEquals("2009-05-10", response.getReleased()),
                () -> assertEquals(4.44, response.getRating()),
                () -> assertEquals(83, response.getMetacritic()),
                () -> assertEquals("https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg", response.getBackgroundImage())
        );
        server.verify();
    }

    @Test
    void shouldThrowBadGatewayWhenGetGameByIdReturnsEmptyBody() {
        server.expect(requestTo("http://localhost:8081/api/games/22509"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.getGameById(22509));
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }

    @Test
    void shouldThrowNotFoundWhenGameDoesNotExist() {
        server.expect(requestTo("http://localhost:8081/api/games/22509"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.getGameById(22509));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        server.verify();
    }

    @Test
    void shouldThrowBadGatewayWhenGetGameByIdReturns500() {
        server.expect(requestTo("http://localhost:8081/api/games/22509"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseStatusException exception  = assertThrows(ResponseStatusException.class, () -> connector.getGameById(22509));
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }
}
