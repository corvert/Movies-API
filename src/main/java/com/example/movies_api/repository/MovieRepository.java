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


    Optional<Movie> findByMovieTitle(String movieTitle);

    @Query("SELECT m FROM Movie m JOIN m.genreSet g WHERE LOWER(g.genreName) = LOWER(:genreName)")
    List<Movie> findByGenreSet_genreNameIgnoreCase(@Param("genreName") String genreName);

    List<Movie> findByReleaseYear(Long releaseYear);

    boolean existsByReleaseYear(Long releaseYear);

    @Query("SELECT g.movieSet FROM Genre g WHERE g.genreId = :genreId")
    Set<Movie> findMoviesByGenreId(Long genreId);


    @Query("SELECT COUNT(m) FROM Movie m JOIN m.genreSet g WHERE g.genreId = :genreId")
    int countMoviesByGenreId(@Param("genreId") Long genreId);

    @Query("SELECT COUNT(m) FROM Movie m JOIN m.actorSet a WHERE a.actorId = :actorId")
    int countMoviesByActorId(@Param("actorId") Long actorId);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.movieTitle) LIKE LOWER(CONCAT('%', :someName, '%'))")
    Set<Movie> findByPartialMovieTitle(@Param("someName") String someName);


    List<Movie> findByMovieTitleAndMovieIdNot(String movieTitle, Long movieId);
}
