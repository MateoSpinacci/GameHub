package org.example.msgames.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.msgames.controller.GamesController;
import org.example.msgames.dto.GamesDTO;
import org.example.msgames.interfaces.GamesConnector;
import org.example.msgames.model.Games;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GamesService {

    GamesConnector gamesConnector;

    public List<GamesDTO> listar(String nombre) {
        return gamesConnector.listar(nombre)
                .stream()
                .map(GamesDTO::of)
                .toList();
    }

    public GamesDTO juegoPorId(Integer id) {
        Games game = gamesConnector.juegoPorId(id);
        if (game == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe un juego con ese ID"
            );
        }
        return GamesDTO.of(game);
    }
}
