package org.example.mslibrary;

import org.example.mslibrary.connector.MsGamesConnector;
import org.example.mslibrary.dto.*;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private LibraryEntry createLibraryEntry() {
        LibraryEntry entry = new LibraryEntry();
        entry.setId(1L);
        entry.setGameId(22509);
        entry.setStatus(GameStatus.PLAYING);
        entry.setPersonalRating(10);
        entry.setAddedAt(LocalDate.of(2020, 1, 1));
        return entry;
    }

    private void assertGameEquals(GetResponse expected, GetResponse actual) {
        assertAll(
                () -> assertEquals(expected.getId(), actual.getId()),
                () -> assertEquals(expected.getGameId(), actual.getGameId()),
                () -> assertEquals(expected.getAddedAt(), actual.getAddedAt()),
                () -> assertEquals(expected.getName(), actual.getName()),
                () -> assertEquals(expected.getStatus(), actual.getStatus()),
                () -> assertEquals(expected.getBackgroundImage(), actual.getBackgroundImage()),
                () -> assertEquals(expected.getPersonalRating(), actual.getPersonalRating())
        );
    }

    @Test
    void addGameCorrectly() {

        LibraryEntry entry = createLibraryEntry();

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
                LocalDate.of(2020,1, 1)
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
    void getAllGamesCorrectly() {
        List<LibraryEntry> entries = new ArrayList<>();
        entries.add(createLibraryEntry());
        entries.add(new LibraryEntry(
                2L,
                3498,
                GameStatus.WANT_TO_PLAY,
                9,
                LocalDate.of(2025, 12, 15)
        ));

        RAWGResponse responseMinecraft = new RAWGResponse(
                22509,
                "Minecraft",
                "2009-05-10",
                4.44,
                83,
                "https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg"
        );

        RAWGResponse responseGta = new RAWGResponse(
                3498,
                "Grand Theft Auto V",
                "2013-09-17",
                4.44,
                92,
                "https://media.rawg.io/media/games/20a/20aa03a10cda45239fe22d035c0ebe64.jpg"
        );

        List<GetResponse> responses = new ArrayList<>();
        responses.add(new GetResponse(
                1L,
                22509,
                "Minecraft",
                "https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg",
                GameStatus.PLAYING,
                10,
                LocalDate.of(2020, 1, 1)
        ));
        responses.add(new GetResponse(
                2L,
                3498,
                "Grand Theft Auto V",
                "https://media.rawg.io/media/games/20a/20aa03a10cda45239fe22d035c0ebe64.jpg",
                GameStatus.WANT_TO_PLAY,
                9,
                LocalDate.of(2025, 12, 15)
        ));

        when(repository.findAll()).thenReturn(entries);
        when(connector.getJuegoPorId(22509)).thenReturn(responseMinecraft);
        when(connector.getJuegoPorId(3498)).thenReturn(responseGta);

        List<GetResponse> results = service.getAllGames();

        assertEquals(2, results.size());
        assertAll(
                () -> assertGameEquals(responses.get(0), results.get(0)),
                () -> assertGameEquals(responses.get(1), results.get(1))
        );
        verify(repository).findAll();
        verify(connector).getJuegoPorId(22509);
        verify(connector).getJuegoPorId(3498);
        verifyNoMoreInteractions(connector);
    }

    @Test
    void getEmptyLibrary() {
        when(repository.findAll()).thenReturn(List.of());

        List<GetResponse> result = service.getAllGames();

        assertTrue(result.isEmpty());
        verify(repository).findAll();
        verifyNoInteractions(connector);
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

    @Test
    void getGameByIdCorrectly() {
        LibraryEntry entry = createLibraryEntry();

        RAWGResponse rawgResponse = new RAWGResponse();
        rawgResponse.setId(22509);
        rawgResponse.setName("Minecraft");
        rawgResponse.setReleased("2009-05-10");
        rawgResponse.setRating(4.44);
        rawgResponse.setMetacritic(83);
        rawgResponse.setBackgroundImage("https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg");

        GetResponse response = new GetResponse(
                1L,
                22509,
                "Minecraft",
                "https://media.rawg.io/media/games/b4e/b4e4c73d5aa4ec66bbf75375c4847a2b.jpg",
                GameStatus.PLAYING,
                10,
                LocalDate.of(2020, 1, 1)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(entry));
        when(connector.getJuegoPorId(entry.getGameId())).thenReturn(rawgResponse);

        GetResponse getResponse = service.getGame(1L);

        assertAll(
                () -> assertEquals(response.getId(), getResponse.getId()),
                () -> assertEquals(response.getGameId(), getResponse.getGameId()),
                () -> assertEquals(response.getName(), getResponse.getName()),
                () -> assertEquals(response.getBackgroundImage(), getResponse.getBackgroundImage()),
                () -> assertEquals(response.getStatus(), getResponse.getStatus()),
                () -> assertEquals(response.getPersonalRating(), getResponse.getPersonalRating()),
                () -> assertEquals(response.getAddedAt(), getResponse.getAddedAt())
        );
    }

    @Test
    void getGameByNonExistentId() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.getGame(1L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(connector);
    }


    @Test
    void DeleteGameCorrectly() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteGame(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void DeleteNonExistentGame() {

        when(repository.existsById(1L)).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.deleteGame(1L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository, never()).deleteById(1L);
    }

    @Test
    void updateGameCorrectly() {
        LibraryEntry entry = createLibraryEntry();

        PatchEntryDTO dto = new PatchEntryDTO(
                GameStatus.COMPLETED,
                9
        );

        LibraryResponse response = new LibraryResponse(
                1L,
                22509,
                GameStatus.COMPLETED,
                9,
                LocalDate.of(2020, 1, 1)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(entry));
        when(repository.save(any(LibraryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LibraryResponse resultado = service.updateGame(1L, dto);

        verify(repository).save(any(LibraryEntry.class));
        assertAll(
                () -> assertEquals(response.getId(), resultado.getId()),
                () -> assertEquals(response.getGameId(), resultado.getGameId()),
                () -> assertEquals(response.getStatus(), resultado.getStatus()),
                () -> assertEquals(response.getPersonalRating(), resultado.getPersonalRating()),
                () -> assertEquals(response.getAddedAt(), resultado.getAddedAt())
        );
    }

    @Test
    void updateOnlyStatusCorrectly() {
        LibraryEntry entry = createLibraryEntry();

        PatchEntryDTO dto = new PatchEntryDTO(
                GameStatus.COMPLETED,
                null
        );

        LibraryResponse response = new LibraryResponse(
                1L,
                22509,
                GameStatus.COMPLETED,
                10,
                LocalDate.of(2020, 1, 1)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(entry));
        when(repository.save(any(LibraryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LibraryResponse resultado = service.updateGame(1L, dto);

        verify(repository).save(any(LibraryEntry.class));
        assertAll(
                () -> assertEquals(response.getId(), resultado.getId()),
                () -> assertEquals(response.getGameId(), resultado.getGameId()),
                () -> assertEquals(response.getStatus(), resultado.getStatus()),
                () -> assertEquals(response.getPersonalRating(), resultado.getPersonalRating()),
                () -> assertEquals(response.getAddedAt(), resultado.getAddedAt())
        );
    }

    @Test
    void updateOnlyRatingCorrectly() {
        LibraryEntry entry = createLibraryEntry();

        PatchEntryDTO dto = new PatchEntryDTO(
                null,
                4
        );

        LibraryResponse response = new LibraryResponse(
                1L,
                22509,
                GameStatus.PLAYING,
                4,
                LocalDate.of(2020, 1, 1)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(entry));
        when(repository.save(any(LibraryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LibraryResponse resultado = service.updateGame(1L, dto);

        verify(repository).save(any(LibraryEntry.class));
        assertAll(
                () -> assertEquals(response.getId(), resultado.getId()),
                () -> assertEquals(response.getGameId(), resultado.getGameId()),
                () -> assertEquals(response.getStatus(), resultado.getStatus()),
                () -> assertEquals(response.getPersonalRating(), resultado.getPersonalRating()),
                () -> assertEquals(response.getAddedAt(), resultado.getAddedAt())
        );
    }

    @Test
    void tryToUpdateWithBothNullArguments() {
        PatchEntryDTO dto = new PatchEntryDTO(
                null,
                null
        );

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.updateGame(1L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void updateNonExistentGame() {
        PatchEntryDTO dto = new PatchEntryDTO(
                GameStatus.DROPPED,
                6
        );

        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.updateGame(1L, dto));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository, never()).save(any(LibraryEntry.class));
        verify(repository).findById(1L);
    }
}
