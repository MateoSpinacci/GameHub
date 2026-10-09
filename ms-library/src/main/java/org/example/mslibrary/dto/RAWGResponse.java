package org.example.mslibrary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RAWGResponse {
    Integer id;
    String name;
    String released;
    Double rating;
    Integer metacritic;

    @JsonProperty("background_image")
    String backgroundImage;
}
