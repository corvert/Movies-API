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

    // Saves a new genre after checking if it already exists
    public Genre save(Genre genre) throws BadRequestException {
        Optional<Genre> genreExits = genreRepository.findByGenreNameIgnoreCase(genre.getGenreName());
        if (genreExits.isPresent()) {
            throw new BadRequestException("Genre " + genre.getGenreName() + " already exits");
        }
        return genreRepository.save(genre);
    }

    // Retrieves all genres sorted by name
    public List<Genre> getAllGenres() {
        List<Genre> genreList = genreRepository.findAll();
        return genreList.stream()
                .sorted((g1, g2) -> g1.getGenreName().compareTo(g2.getGenreName())) // Sort genres by name
                .collect(Collectors.toList());
    }

    // Finds a genre by its ID, throwing an exception if not found
    public Genre findGenreById(Long id) {
        boolean idExists = genreRepository.existsById(id);
        if (!idExists) {
            throw new ResourceNotFoundException("Genre with id " + id + " does not exists");
        }
        return genreRepository.findById(id).orElseThrow();
    }

    // Retrieves movies associated with a specific genre by name
    public Set<Movie> getMoviesByGenre(String genreName) {
        return genreRepository.findMoviesByGenreName(genreName);
    }

    // Updates the name of an existing genre if it is different from the current name
    public void updateGenre(Long genreId, String genreName) {
        Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new ResourceNotFoundException(
                "Genre with id " + genreId + " does not exists"
        ));
        // Check if the new name is valid and different from the current name
        if (genreName != null && genreName.length() > 0 && !Objects.equals(genre.getGenreName(), genreName)) {
            genre.setGenreName(genreName); // Update genre name
            genreRepository.save(genre); // Save the updated genre
        }
    }

    // Deletes a genre by its ID, checking for associated movies unless forced
    @Transactional
    public void deleteGenre(Long genreId, boolean force) throws BadRequestException {
        boolean exists = genreRepository.existsById(genreId);
        if (!exists) {
            throw new ResourceNotFoundException("Genre with id " + genreId + " does not exists");
        }
        // Count how many movies are associated with the genre
        int associatedMoviesCount = movieRepository.countMoviesByGenreId(genreId);
        // Prevent deletion if there are associated movies unless forced
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
