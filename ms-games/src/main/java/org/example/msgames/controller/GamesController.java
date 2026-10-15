package org.example.msgames.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.example.msgames.dto.GamesDTO;
import org.example.msgames.service.GamesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GamesController {

    GamesService gamesService;

    @GetMapping
    public ResponseEntity<List<GamesDTO>> listar(@RequestParam String nombre) {
        val juegos = gamesService.listar(nombre);

        return ResponseEntity.ok(juegos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GamesDTO> juegoPorId(@PathVariable Integer id) {
        val juego = gamesService.juegoPorId(id);

        return ResponseEntity.ok(juego);
    }
}
