package com.example.movies_api.repository;

import com.example.movies_api.entities.Genre;
import com.example.movies_api.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    // Finds all genres that match a specific genre name
    List<Genre> findAllByGenreName(String genreName);

    // Finds all genres that match a specific genre ID
    List<Genre> findAllByGenreId(Long genreId);

    // Retrieves a set of movies associated with a specific genre name, case-insensitively
    @Query("SELECT g.movieSet FROM Genre g WHERE LOWER(g.genreName) = LOWER(:genreName)")
    Set<Movie> findMoviesByGenreName(String genreName);

    // Finds a genre by its name, returning an Optional to handle the case where the genre may not exist
    Optional<Genre> findByGenreName(String genreName);

    // Counts the number of genres associated with a specific movie by its ID
    @Query("SELECT COUNT(g) FROM Movie m JOIN m.genreSet g WHERE m.movieId = :movieId")
    int countGenresByMovieId(@Param("movieId") Long movieId);

    // Retrieves a set of genres associated with a specific movie by its ID
    @Query("SELECT g FROM Genre g JOIN g.movieSet m WHERE m.movieId = :movieId")
    Set<Genre> findGenresByMovieId(@Param("movieId") Long movieId);

    // Finds a genre by its name, ignoring case sensitivity
    Optional<Genre> findByGenreNameIgnoreCase(String genreName);
}
