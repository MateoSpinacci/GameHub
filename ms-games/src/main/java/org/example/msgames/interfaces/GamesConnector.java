package org.example.msgames.interfaces;

import org.example.msgames.model.Games;
import java.util.List;

public interface GamesConnector {
    List<Games> listar(String nombre);

    Games juegoPorId(Integer id);
}
