package org.example.msgames.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Games {
    private Integer id;
    private String name;
    private String released;
    private Double rating;
    private Integer metacritic;

    @JsonProperty("background_image")
    private String backgroundImage;
}
