package org.example.msgames.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.msgames.controller.GamesController;
import org.example.msgames.dto.GamesDTO;
import org.example.msgames.interfaces.GamesConnector;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        return GamesDTO.of(gamesConnector.juegoPorId(id));
    }
}
