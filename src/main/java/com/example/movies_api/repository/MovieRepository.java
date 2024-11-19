package com.example.movies_api.repository;

import com.example.movies_api.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Finds a movie by its title, returning an Optional to handle the case where the movie may not exist
    Optional<Movie> findByMovieTitle(String movieTitle);

    // Retrieves a list of movies associated with a specific genre name, case-insensitively
    @Query("SELECT m FROM Movie m JOIN m.genreSet g WHERE LOWER(g.genreName) = LOWER(:genreName)")
    List<Movie> findByGenreSet_genreNameIgnoreCase(@Param("genreName") String genreName);

    // Finds all movies released in a specific year
    List<Movie> findByReleaseYear(Long releaseYear);

    // Checks if any movies exist for a specific release year
    boolean existsByReleaseYear(Long releaseYear);

    // Retrieves a set of movies associated with a specific genre ID
    @Query("SELECT g.movieSet FROM Genre g WHERE g.genreId = :genreId")
    Set<Movie> findMoviesByGenreId(Long genreId);

    // Counts the number of movies associated with a specific genre by its ID
    @Query("SELECT COUNT(m) FROM Movie m JOIN m.genreSet g WHERE g.genreId = :genreId")
    int countMoviesByGenreId(@Param("genreId") Long genreId);

    // Counts the number of movies associated with a specific actor by their ID
    @Query("SELECT COUNT(m) FROM Movie m JOIN m.actorSet a WHERE a.actorId = :actorId")
    int countMoviesByActorId(@Param("actorId") Long actorId);

    // Finds movies whose titles contain the specified substring, case-insensitively
    @Query("SELECT m FROM Movie m WHERE LOWER(m.movieTitle) LIKE LOWER(CONCAT('%', :someName, '%'))")
    Set<Movie> findByPartialMovieTitle(@Param("someName") String someName);

    // Finds movies with a specific title, excluding a movie with a given ID (useful for updating)
    List<Movie> findByMovieTitleAndMovieIdNot(String movieTitle, Long movieId);
}
