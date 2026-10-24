package org.example.mslibrary;

import org.example.mslibrary.config.SecurityConfig;
import org.example.mslibrary.controller.LibraryController;
import org.example.mslibrary.dto.GetResponse;
import org.example.mslibrary.dto.LibraryEntryDTO;
import org.example.mslibrary.dto.LibraryResponse;
import org.example.mslibrary.dto.PatchEntryDTO;
import org.example.mslibrary.model.GameStatus;
import org.example.mslibrary.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
@Import(SecurityConfig.class)
public class MsLibraryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService service;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/library"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void shouldAllowAuthenticatedUserToGetLibrary() throws Exception {
        when(service.getAllGames(any(Jwt.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/library")
                .with(jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(status().isOk());

        verify(service).getAllGames(any(Jwt.class));
    }

    @Test
    void shouldAllowAuthenticatedUserToDeleteGame() throws Exception {
        mockMvc.perform(delete("/api/library/1")
                .with(jwt()
                        .jwt(token -> token.subject("usuario-123"))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(status().isNoContent());

        ArgumentCaptor<Jwt> jwtCaptor = ArgumentCaptor.forClass(Jwt.class);

        verify(service).deleteGame(eq(1L), jwtCaptor.capture());
        assertEquals("usuario-123", jwtCaptor.getValue().getSubject());
    }

    @Test
    void shouldAllowAndGetTheCorrectJson() throws Exception {
        GetResponse response = new GetResponse(
                1L,
                22509,
                "Minecraft",
                "https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg",
                GameStatus.PLAYING,
                10,
                LocalDate.of(2020, 1, 1)
        );

        when(service.getGame(eq(1L), any(Jwt.class))).thenReturn(response);

        mockMvc.perform(get("/api/library/1")
                .with(jwt()
                        .jwt(token -> token.subject("usuario-123"))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(jsonPath("$.name").value("Minecraft"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.background_image").value("https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg"))
                .andExpect(status().isOk());

        ArgumentCaptor<Jwt> jwtCaptor = ArgumentCaptor.forClass(Jwt.class);

        verify(service).getGame(eq(1L), jwtCaptor.capture());
        assertEquals("usuario-123", jwtCaptor.getValue().getSubject());
    }

    @Test
    void shouldCreateLibraryEntryAndReturnCreatedStatus() throws Exception {
        String json = """
                {
                "gameId": 22509,
                "status": "PLAYING",
                "personalRating": 10
                }
                """;

        LibraryResponse response = new LibraryResponse(
                1L,
                22509,
                GameStatus.PLAYING,
                10,
                LocalDate.of(2020, 1, 1)
        );

        when(service.addGame(any(LibraryEntryDTO.class), any(Jwt.class))).thenReturn(response);

        mockMvc.perform(post("/api/library")
                        .with(jwt()
                                .jwt(token -> token.subject("usuario-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.gameId").value(22509))
                .andExpect(jsonPath("$.personalRating").value(10))
                .andExpect(status().isCreated());

        ArgumentCaptor<Jwt> jwtCaptor = ArgumentCaptor.forClass(Jwt.class);
        ArgumentCaptor<LibraryEntryDTO> dtoCaptor = ArgumentCaptor.forClass(LibraryEntryDTO.class);

        verify(service).addGame(dtoCaptor.capture(), jwtCaptor.capture());
        assertEquals("usuario-123", jwtCaptor.getValue().getSubject());
    }

    @Test
    void shouldUpdateLibraryEntryAndReturnOk() throws Exception {
        String json = """
                {
                "personalRating": 8
                }
                """;

        LibraryResponse response = new LibraryResponse(
                1L,
                22509,
                GameStatus.PLAYING,
                8,
                LocalDate.of(2020, 1, 1)
        );

        when(service.updateGame(eq(1L), any(PatchEntryDTO.class), any(Jwt.class))).thenReturn(response);

        mockMvc.perform(patch("/api/library/1")
                        .with(jwt()
                                .jwt(token -> token.subject("usuario-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.gameId").value(22509))
                .andExpect(jsonPath("$.personalRating").value(8))
                .andExpect(status().isOk());

        ArgumentCaptor<Jwt> jwtCaptor = ArgumentCaptor.forClass(Jwt.class);
        ArgumentCaptor<PatchEntryDTO> dtoCaptor = ArgumentCaptor.forClass(PatchEntryDTO.class);

        verify(service).updateGame(eq(1L), dtoCaptor.capture(), jwtCaptor.capture());
        assertEquals("usuario-123", jwtCaptor.getValue().getSubject());
        assertEquals(8, dtoCaptor.getValue().getPersonalRating());
    }

    @Test
    void shouldRejectInvalidLibraryEntryAndReturnBadRequest() throws Exception {
        String json = """
                {
                "gameId": 22509,
                "status": "PLAYING",
                "personalRating": 15
                }
                """;

        mockMvc.perform(post("/api/library")
                        .with(jwt()
                                .jwt(token -> token.subject("usuario-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturnNotFoundWhenLibraryEntryDoesNotExist() throws Exception {
        when(service.getGame(eq(999L), any(Jwt.class)))
                .thenThrow(new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe un juego con ese ID en tu biblioteca"
                ));

        mockMvc.perform(get("/api/library/999")
                        .with(jwt()
                                .jwt(token -> token.subject("usuario-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isNotFound());

        verify(service).getGame(eq(999L), any(Jwt.class));
    }

    @Test
    void shouldRejectUserWithoutRequiredRole() throws Exception {
        mockMvc.perform(get("/api/library")
                        .with(jwt()
                                .jwt(token -> token.subject("usuario-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }
}
