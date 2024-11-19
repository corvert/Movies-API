package com.example.movies_api.repository;

import com.example.movies_api.entities.Actor;
import com.example.movies_api.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {


    Optional<Actor> findByActorName(String actorName);

    @Query("SELECT a.movieSet FROM Actor a WHERE a.actorId = :actorId")
    Set<Movie> findMoviesByActorId(Long actorId);

    @Query("SELECT a FROM Actor a WHERE LOWER(a.actorName) LIKE LOWER(CONCAT('%', :someName, '%'))")
    Set<Actor> findByPartialActorName(@Param("someName") String someName);

    @Query("SELECT COUNT(a) FROM Movie m JOIN m.actorSet a WHERE m.movieId = :movieId")
    int countActorsByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT a FROM Actor a JOIN a.movieSet m WHERE m.movieId = :movieId")
    Set<Actor> findActorsByMovieId(@Param("movieId") Long movieId);

}
