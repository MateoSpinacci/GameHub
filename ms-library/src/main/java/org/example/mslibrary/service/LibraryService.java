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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class LibraryService {
    LibraryRepository repository;
    MsGamesConnector connector;

    public LibraryResponse addGame(LibraryEntryDTO dto, Jwt jwt) {
        if (repository.existsByUserIdAndGameId(jwt.getSubject(), dto.getGameId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Game already exists in your library"
            );
        }
        if (!connector.existsById(dto.getGameId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Game not found in RAWG"
            );
        }
        LibraryEntry entry = LibraryEntry.of(dto, jwt);
        LibraryEntry savedEntry = repository.save(entry);
        return LibraryResponse.of(savedEntry);
    }

    public List<GetResponse> getAllGames(Jwt jwt) {
        return repository.findByUserId(jwt.getSubject())
                .stream()
                .map(r -> GetResponse.of(r, connector.getGameById(r.getGameId())))
                .toList();
    }

    public GetResponse getGame(Long id, Jwt jwt) {
        LibraryEntry entry = repository.findByIdAndUserId(id, jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Game not found in your library"
                ));

        return GetResponse.of(entry, connector.getGameById(entry.getGameId()));
    }

    public void deleteGame(Long id, Jwt jwt) {
        LibraryEntry entry = repository.findByIdAndUserId(id, jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Game not found in your library"
                ));
        repository.delete(entry);
    }

    public LibraryResponse updateGame(Long id, PatchEntryDTO dto, Jwt jwt) {
        if (dto.getPersonalRating() == null && dto.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided for update"
            );
        }
        LibraryEntry entry = repository.findByIdAndUserId(id, jwt.getSubject())
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Game not found in your library"
                        )
                );
        if (dto.getPersonalRating() != null) {
            entry.setPersonalRating(dto.getPersonalRating());
        }
        if (dto.getStatus() != null) {
            entry.setStatus(dto.getStatus());
        }
        LibraryEntry updatedEntry = repository.save(entry);
        return LibraryResponse.of(updatedEntry);
    }
}
