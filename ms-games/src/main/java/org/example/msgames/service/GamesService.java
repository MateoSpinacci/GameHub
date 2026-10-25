package org.example.msgames.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.msgames.dto.GamesDTO;
import org.example.msgames.interfaces.GamesConnector;
import org.example.msgames.model.Games;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GamesService {

    GamesConnector gamesConnector;

    public List<GamesDTO> listGames(String name) {
        return gamesConnector.list(name)
                .stream()
                .map(GamesDTO::of)
                .toList();
    }

    public GamesDTO getGameById(Integer id) {
        Games game = gamesConnector.gameById(id);

        return GamesDTO.of(game);
    }
}
