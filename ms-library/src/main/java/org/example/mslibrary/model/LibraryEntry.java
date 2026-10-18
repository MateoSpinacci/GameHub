package org.example.mslibrary.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.mslibrary.dto.LibraryEntryDTO;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"}))
public class LibraryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "user_id", nullable = false)
    String userId;
    @Column(name = "game_id")
    Integer gameId;
    @Enumerated(EnumType.STRING)
    GameStatus status;
    Integer personalRating;
    LocalDate addedAt;

    private LibraryEntry(String jwt, Integer gameId, GameStatus status, Integer personalRating, LocalDate now) {
        this.userId = jwt;
        this.gameId = gameId;
        this.status = status;
        this.personalRating = personalRating;
        this.addedAt = now;
    }


    public static LibraryEntry of(LibraryEntryDTO dto, Jwt jwt) {
        return new LibraryEntry(
                    jwt.getSubject(),
                    dto.getGameId(),
                    dto.getStatus(),
                    dto.getPersonalRating(),
                    LocalDate.now()
        );
    }
}
