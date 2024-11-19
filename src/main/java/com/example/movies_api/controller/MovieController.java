package com.example.movies_api.controller;

import com.example.movies_api.dto.MovieDTO;
import com.example.movies_api.entities.Actor;
import com.example.movies_api.entities.Movie;
import com.example.movies_api.service.MovieService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;


    @PostMapping("/add-movie")
    public ResponseEntity<Movie> addMovie(@Valid @RequestBody Movie movie) throws BadRequestException {
        return new ResponseEntity<Movie>(movieService.addMovie(movie), HttpStatus.CREATED);
    }

    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<Movie> getMovieById(@PathVariable("movieId") Long movieId) {
        Movie movie = movieService.findMovieById(movieId);
        return ResponseEntity.ok(movie);
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<List<Movie>> filterByGenre(@PathVariable String genreName) {
        List<Movie> movieList = movieService.findMoviesByGenre(genreName);
        return ResponseEntity.ok(movieList);
    }

    @GetMapping("/year/{releaseYear}")
    public ResponseEntity<List<Movie>> filterByReleaseYear(@PathVariable Long releaseYear) {
        return new ResponseEntity<List<Movie>>(movieService.findMoviesByReleaseYear(releaseYear), HttpStatus.OK);
    }

    @GetMapping("/{movieId}/actors")
    public ResponseEntity<Set<Actor>> getActorsByMovie(@PathVariable Long movieId) {
        return new ResponseEntity<Set<Actor>>(movieService.findActorsByMovie(movieId), HttpStatus.OK);
    }

    @PatchMapping("/update/{movieId}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long movieId, @RequestBody MovieDTO movieDTO) throws BadRequestException {
        Movie updatedMovie = movieService.updateMovie(movieId, movieDTO.getMovieTitle(), movieDTO.getReleaseYear(), movieDTO.getDuration(),
                movieDTO.getGenreSet(), movieDTO.getActorSet(), movieDTO.getActorsToRemove(), movieDTO.getGenresToRemove());
        return ResponseEntity.ok(updatedMovie);
    }

    @GetMapping("/movies/{genreId}")
    public ResponseEntity<Set<Movie>> getMoviesByGenreId(@PathVariable Long genreId) {
        return new ResponseEntity<Set<Movie>>(movieService.getMoviesByGenreId(genreId), HttpStatus.OK);

    }

    @DeleteMapping("{movieId}")
    @Transactional
    public ResponseEntity<?> deleteMovie(@PathVariable Long movieId, @RequestParam(name = "force", defaultValue = "false") boolean force) {
        try {
            movieService.deleteMovie(movieId, force);
            return ResponseEntity.noContent().build();
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search/{someName}")
    public ResponseEntity<Set<Movie>> findMovies(@PathVariable String someName) {
        return new ResponseEntity<>(movieService.findMoviesByPartialName(someName), HttpStatus.OK);
    }

    @GetMapping("/page")
    public Page<Movie> getMoviesByPage(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) throws BadRequestException {
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Size cannot be zero or negative");
        }
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return movieService.getMovies(pageable);
    }

}
