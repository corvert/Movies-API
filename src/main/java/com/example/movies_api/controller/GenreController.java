package com.example.movies_api.controller;


import com.example.movies_api.entities.Genre;
import com.example.movies_api.entities.Movie;
import com.example.movies_api.service.GenreService;
import com.example.movies_api.service.MovieService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/genre")
public class GenreController {
    @Autowired
    private GenreService genreService;
    @Autowired
    private MovieService movieService;


    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres() {
        return new ResponseEntity<List<Genre>>(genreService.getAllGenres(), HttpStatus.OK);
    }

    @PostMapping("/add-genre")
    public ResponseEntity<Genre> addGenre(@Valid @RequestBody Genre genre) throws BadRequestException {
        genreService.save(genre);
        return new ResponseEntity<Genre>(genre, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> findGenreById(@PathVariable("id") Long id) {
        Genre genre = genreService.findGenreById(id);
        return ResponseEntity.ok(genre);
    }

    @GetMapping("/{genreName}/movies")
    public Set<Movie> getMoviesByGenre(@PathVariable String genreName) {
        return genreService.getMoviesByGenre(genreName);
    }

    @PatchMapping("/update/{genreId}")
    public ResponseEntity<Genre> updateGenre(@PathVariable("genreId") Long genreId, @RequestBody Genre genre) {
        genreService.updateGenre(genreId, genre.getGenreName());
        return ResponseEntity.ok(genre);
    }

    @DeleteMapping("{genreId}")
    @Transactional
    public ResponseEntity<?> deleteGenre(@PathVariable("genreId") Long genreId, @RequestParam(name = "force", defaultValue = "false") boolean force) {
        try {
            genreService.deleteGenre(genreId, force);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Genre is deleted");
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
