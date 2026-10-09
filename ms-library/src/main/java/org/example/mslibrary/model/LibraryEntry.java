package org.example.mslibrary.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.mslibrary.dto.LibraryEntryDTO;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
public class LibraryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    Integer gameId;
    @Enumerated(EnumType.STRING)
    GameStatus status;
    Integer personalRating;
    LocalDate addedAt;

    private LibraryEntry(Integer gameId, GameStatus status, Integer personalRating, LocalDate now) {
        this.gameId = gameId;
        this.status = status;
        this.personalRating = personalRating;
        this.addedAt = now;
    }


    public static LibraryEntry of(LibraryEntryDTO dto) {
        return new LibraryEntry(
                    dto.getGameId(),
                    dto.getStatus(),
                    dto.getPersonalRating(),
                    LocalDate.now()
        );
    }
}
