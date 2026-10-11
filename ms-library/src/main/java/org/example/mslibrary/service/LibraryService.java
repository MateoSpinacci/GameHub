package org.example.mslibrary.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.mslibrary.dto.GetResponse;
import org.example.mslibrary.dto.LibraryEntryDTO;
import org.example.mslibrary.dto.LibraryResponse;
import org.example.mslibrary.dto.PatchEntryDTO;
import org.example.mslibrary.model.LibraryEntry;
import org.example.mslibrary.connector.MsGamesConnector;
import org.example.mslibrary.repository.LibraryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class LibraryService {
    LibraryRepository repository;
    MsGamesConnector connector;

    public LibraryResponse addGame(LibraryEntryDTO dto) {
        if (repository.existsByGameId(dto.getGameId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El juego ya existe en la base de datos"
            );
        }
        if (connector.existeJuegoPorId(dto.getGameId())) {
            LibraryEntry entry = LibraryEntry.of(dto);
            LibraryEntry guardado = repository.save(entry);
            return LibraryResponse.of(guardado);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El juego no existe en RAWG"
            );
        }
    }

    public List<GetResponse> getAllGames() {
        return repository.findAll()
                .stream()
                .map(r -> GetResponse.of(r, connector.getJuegoPorId(r.getGameId())))
                .toList();
    }

    public GetResponse getGame(Long id) {
        LibraryEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un juego con ese ID en tu biblioteca"
                ));

        return GetResponse.of(entry, connector.getJuegoPorId(entry.getGameId()));
    }

    public void deleteGame(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe un juego con ese ID en tu biblioteca"
            );
        }
        repository.deleteById(id);
    }

    public LibraryResponse updateGame(Long id, PatchEntryDTO dto) {
        if (dto.getPersonalRating() == null && dto.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe proporcionar al menos un campo para actualizar"
            );
        }
        LibraryEntry juego = repository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No existe un juego con ese ID en tu biblioteca"
                        )
                );
        if (dto.getPersonalRating() != null) {
            juego.setPersonalRating(dto.getPersonalRating());
        }
        if (dto.getStatus() != null) {
            juego.setStatus(dto.getStatus());
        }
        LibraryEntry cambio = repository.save(juego);
        return LibraryResponse.of(cambio);
    }
}
