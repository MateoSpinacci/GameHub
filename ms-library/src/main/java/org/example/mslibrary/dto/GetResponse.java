package org.example.mslibrary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.mslibrary.model.GameStatus;
import org.example.mslibrary.model.LibraryEntry;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetResponse {
    Long id;
    Integer gameId;
    String name;
    @JsonProperty("background_image")
    String backgroundImage;
    GameStatus status;
    Integer personalRating;
    LocalDate addedAt;

    public static GetResponse of(LibraryEntry entry, RAWGResponse rawgResponse) {
        return new GetResponse(
                entry.getId(),
                entry.getGameId(),
                rawgResponse.getName(),
                rawgResponse.getBackgroundImage(),
                entry.getStatus(),
                entry.getPersonalRating(),
                entry.getAddedAt()
        );
    }
}
