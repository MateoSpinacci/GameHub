package org.example.mslibrary;

import org.example.mslibrary.connector.MsGamesConnector;
import org.example.mslibrary.dto.LibraryEntryDTO;
import org.example.mslibrary.dto.LibraryResponse;
import org.example.mslibrary.model.GameStatus;
import org.example.mslibrary.model.LibraryEntry;
import org.example.mslibrary.repository.LibraryRepository;
import org.example.mslibrary.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MsLibraryServiceTests {

    @Mock
    LibraryRepository repository;

    @Mock
    MsGamesConnector connector;

    @InjectMocks
    LibraryService service;

    @Test
    void addGameCorrectly() {

        LibraryEntry entry = new LibraryEntry();
        entry.setId(1L);
        entry.setGameId(22509);
        entry.setStatus(GameStatus.PLAYING);
        entry.setPersonalRating(10);
        entry.setAddedAt(LocalDate.now());

        LibraryEntryDTO dto = new LibraryEntryDTO(
                22509,
                GameStatus.PLAYING,
                10
        );

        LibraryResponse response = new LibraryResponse(
                1L,
                22509,
                GameStatus.PLAYING,
                10,
                LocalDate.now()
        );

        when(repository.existsByGameId(22509)).thenReturn(false);
        when(connector.existeJuegoPorId(22509)).thenReturn(true);
        when(repository.save(any(LibraryEntry.class))).thenReturn(entry);

        LibraryResponse resultado = service.addGame(dto);

        verify(repository, times(1)).save(any(LibraryEntry.class));
        verify(connector).existeJuegoPorId(22509);

        assertAll(
                () -> assertEquals(response.getId(), resultado.getId()),
                () -> assertEquals(response.getGameId(), resultado.getGameId()),
                () -> assertEquals(response.getStatus(), resultado.getStatus()),
                () -> assertEquals(response.getPersonalRating(), resultado.getPersonalRating()),
                () -> assertEquals(response.getAddedAt(), resultado.getAddedAt())
        );
    }

    @Test
    void addDuplicatedGame() {
        LibraryEntryDTO dto = new LibraryEntryDTO(
                22509,
                GameStatus.PLAYING,
                10
        );

        when(repository.existsByGameId(22509)).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.addGame(dto));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any(LibraryEntry.class));
        verify(connector, never()).existeJuegoPorId(anyInt());
    }

    @Test
    void addNonExistentGame() {
        LibraryEntryDTO dto = new LibraryEntryDTO(
                225012939,
                GameStatus.PLAYING,
                10
        );

        when(repository.existsByGameId(225012939)).thenReturn(false);
        when(connector.existeJuegoPorId(225012939)).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.addGame(dto));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository, never()).save(any(LibraryEntry.class));
        verify(connector).existeJuegoPorId(225012939);
    }
}
