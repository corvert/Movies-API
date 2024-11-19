package com.example.movies_api.service;

import com.example.movies_api.entities.Genre;
import com.example.movies_api.entities.Movie;
import com.example.movies_api.exceptions.ResourceNotFoundException;
import com.example.movies_api.repository.GenreRepository;
import com.example.movies_api.repository.MovieRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GenreService {
    @Autowired
    GenreRepository genreRepository;
    @Autowired
    MovieRepository movieRepository;

    public Genre save(Genre genre) throws BadRequestException {
        Optional<Genre> genreExits = genreRepository.findByGenreNameIgnoreCase(genre.getGenreName());
        if (genreExits.isPresent()) {
            throw new BadRequestException("Genre " + genre.getGenreName() + " already exits");
        }
        return genreRepository.save(genre);
    }

    public List<Genre> getAllGenres() {
        List<Genre> genreList = genreRepository.findAll();
        return genreList.stream()
                .sorted((g1, g2) -> g1.getGenreName().compareTo(g2.getGenreName()))
                .collect(Collectors.toList());
    }

    public Genre findGenreById(Long id) {
        boolean idExists = genreRepository.existsById(id);
        if (!idExists) {
            throw new ResourceNotFoundException("Genre with id " + id + " does not exists");
        }
        return genreRepository.findById(id).orElseThrow();
    }

    public Set<Movie> getMoviesByGenre(String genreName) {
        return genreRepository.findMoviesByGenreName(genreName);
    }


    public void updateGenre(Long genreId, String genreName) {
        Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new ResourceNotFoundException(
                "Genre with id " + genreId + " does not exists"
        ));

        if (genreName != null && genreName.length() > 0 && !Objects.equals(genre.getGenreName(), genreName)) {
            genre.setGenreName(genreName);
            genreRepository.save(genre);
        }
    }

    @Transactional
    public void deleteGenre(Long genreId, boolean force) throws BadRequestException {
        boolean exists = genreRepository.existsById(genreId);
        if (!exists) {
            throw new ResourceNotFoundException("Genre with id " + genreId + " does not exists");
        }
        int associatedMoviesCount = movieRepository.countMoviesByGenreId(genreId);

        if (associatedMoviesCount > 0 && !force) {
            throw new BadRequestException("Cannot delete genre " + genreRepository.findById(genreId).get().getGenreName() +
                    " because it has " + associatedMoviesCount + " associated movies.");
        } else {
            // Remove genre from all associated movies
            Set<Movie> movies = movieRepository.findMoviesByGenreId(genreId);
            for (Movie movie : movies) {
                movie.getGenreSet().removeIf(genre -> genre.getGenreId().equals(genreId));
            }
            genreRepository.deleteById(genreId);
        }
    }


}
