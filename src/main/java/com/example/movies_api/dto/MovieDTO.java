package com.example.movies_api.dto;

import com.example.movies_api.entities.Actor;
import com.example.movies_api.entities.Genre;
import lombok.Data;

import java.util.Set;

@Data
public class MovieDTO {
    private String movieTitle;
    private Integer releaseYear;
    private Float duration;
    private Set<Genre> genreSet;
    private Set<Actor> actorSet;
    private Set<Actor> actorsToRemove;
    private Set<Genre> genresToRemove;
}
