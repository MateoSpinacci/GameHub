package org.example.mslibrary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.mslibrary.model.GameStatus;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LibraryEntryDTO {
    @NotNull
    Integer gameId;
    @NotNull
    GameStatus status;
    @Min(1)
    @Max(10)
    Integer personalRating;
}
