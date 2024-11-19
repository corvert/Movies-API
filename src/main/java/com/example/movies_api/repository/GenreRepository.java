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

    List<Genre> findAllByGenreName(String genreName);

    List<Genre> findAllByGenreId(Long genreId);

    @Query("SELECT g.movieSet FROM Genre g WHERE LOWER(g.genreName) = LOWER(:genreName)")
    Set<Movie> findMoviesByGenreName(String genreName);

    Optional<Genre> findByGenreName(String genreName);


    @Query("SELECT COUNT(g) FROM Movie m JOIN m.genreSet g WHERE m.movieId = :movieId")
    int countGenresByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT g FROM Genre g JOIN g.movieSet m WHERE m.movieId = :movieId")
    Set<Genre> findGenresByMovieId(@Param("movieId") Long movieId);

    Optional<Genre> findByGenreNameIgnoreCase(String genreName);
}
