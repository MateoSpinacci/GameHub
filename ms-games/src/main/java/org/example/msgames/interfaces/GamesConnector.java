package org.example.msgames.interfaces;

import org.example.msgames.model.Games;
import java.util.List;

public interface GamesConnector {
    List<Games> list(String nombre);

    Games gameById(Integer id);
}
