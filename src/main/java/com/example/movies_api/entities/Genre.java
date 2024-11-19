package com.example.movies_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "genre")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genre_seq")
    @SequenceGenerator(name = "genre_seq", sequenceName = "genre_sequence", allocationSize = 1)
    private Long genreId;
    @NotBlank(message = "Genre name cannot be blank")
    private String genreName;

    @ManyToMany(mappedBy = "genreSet")
    @JsonIgnore
    private Set<Movie> movieSet = new HashSet<>();
/*
    @JsonProperty("movie")
    public List<Map<String, Object>> getMoviesForSerialization() {
        List<Map<String, Object>> movieList = new ArrayList<>();
        for (Movie movie : movieSet) {
            Map<String, Object> movieInfo = new LinkedHashMap<>();
            movieInfo.put("id", movie.getMovieId());
            movieInfo.put("title", movie.getMovieTitle());
            movieInfo.put("releaseYear", movie.getReleaseYear());
            movieInfo.put("duration", movie.getDuration());
            movieList.add(movieInfo);
        }
        return movieList;
    }

 */

}
