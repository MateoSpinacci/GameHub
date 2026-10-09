package org.example.msgames.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.msgames.model.Games;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GamesDTO {
    private Integer id;
    private String name;
    private String released;
    private Double rating;
    private Integer metacritic;

    @JsonProperty("background_image")
    private String backgroundImage;

    public static GamesDTO of(Games games) {
        return new GamesDTO(
                games.getId(),
                games.getName(),
                games.getReleased(),
                games.getRating(),
                games.getMetacritic(),
                games.getBackgroundImage()
        );
    }
}
