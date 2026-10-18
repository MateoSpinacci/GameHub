package org.example.mslibrary.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.example.mslibrary.dto.GetResponse;
import org.example.mslibrary.dto.LibraryEntryDTO;
import org.example.mslibrary.dto.LibraryResponse;
import org.example.mslibrary.dto.PatchEntryDTO;
import org.example.mslibrary.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/library")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class LibraryController {

    LibraryService service;

    @PostMapping
    public ResponseEntity<LibraryResponse> addGame(@RequestBody @Valid LibraryEntryDTO dto, @AuthenticationPrincipal Jwt jwt) {
        val creado = service.addGame(dto, jwt);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    @GetMapping
    public ResponseEntity<List<GetResponse>> getAllGames(@AuthenticationPrincipal Jwt jwt) {
        val gameList = service.getAllGames(jwt);

        return ResponseEntity.ok(gameList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetResponse> getGame(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        val game = service.getGame(id, jwt);

        return ResponseEntity.ok(game);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.deleteGame(id, jwt);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LibraryResponse> updateGame(@PathVariable Long id, @RequestBody @Valid PatchEntryDTO dto, @AuthenticationPrincipal Jwt jwt) {
        val cambio = service.updateGame(id, dto, jwt);

        return ResponseEntity.ok(cambio);
    }
}
