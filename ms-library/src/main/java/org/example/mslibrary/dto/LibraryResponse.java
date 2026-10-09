package org.example.mslibrary.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.mslibrary.model.GameStatus;
import org.example.mslibrary.model.LibraryEntry;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LibraryResponse {
    Long id;
    Integer gameId;
    GameStatus status;
    Integer personalRating;
    LocalDate addedAt;

    public static LibraryResponse of(LibraryEntry entry) {
        return new LibraryResponse(
                entry.getId(),
                entry.getGameId(),
                entry.getStatus(),
                entry.getPersonalRating(),
                entry.getAddedAt()
        );
    }
}
