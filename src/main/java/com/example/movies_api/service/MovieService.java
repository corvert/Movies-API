package com.example.movies_api.service;

import com.example.movies_api.entities.Actor;
import com.example.movies_api.entities.Genre;
import com.example.movies_api.entities.Movie;
import com.example.movies_api.exceptions.ResourceNotFoundException;
import com.example.movies_api.repository.ActorRepository;
import com.example.movies_api.repository.GenreRepository;
import com.example.movies_api.repository.MovieRepository;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private ActorRepository actorRepository;

    // Adds a new movie to the repository after validating it does not already exist
    @Validated
    public Movie addMovie(@Valid Movie movie) throws BadRequestException {
        // Check if the movie already exists by title
        if (movieRepository.findByMovieTitle(movie.getMovieTitle()).isPresent()) {
            throw new BadRequestException("Movie already exists: " + movie.getMovieTitle());
        }


        // Process genres and actors to ensure they are persisted and linked correctly
        movie.setGenreSet(processGenres(movie.getGenreSet()));
        movie.setActorSet(processActors(movie.getActorSet()));
        return movieRepository.save(movie);
    }

    // Process genres to ensure they are persisted and linked correctly
    private Set<Genre> processGenres(Set<Genre> genres) {
        return genres.stream()
                .map(genre -> {
                    // Check by ID first
                    if (genre.getGenreId() != null) {
                        return genreRepository.findById(genre.getGenreId())
                                .orElseThrow(() -> new ResourceNotFoundException("Genre with ID " + genre.getGenreId() + " does not exist"));
                    }
                    // Then check by name
                    return genreRepository.findByGenreName(genre.getGenreName())
                            .orElseGet(() -> genreRepository.save(genre));
                })
                .collect(Collectors.toSet());
    }

    // Process actors to ensure they are persisted and linked correctly
    private Set<Actor> processActors(Set<Actor> actors) {
        return actors.stream()
                .map(actor -> {
                    // Check by ID first
                    if (actor.getActorId() != null) {
                        return actorRepository.findById(actor.getActorId())
                                .orElseThrow(() -> new ResourceNotFoundException("Actor with ID " + actor.getActorId() + " does not exist"));
                    }
                    // Then check by name
                    return actorRepository.findByActorName(actor.getActorName())
                            .orElseGet(() -> actorRepository.save(actor));
                })
                .collect(Collectors.toSet());
    }


    // Retrieves all movies sorted by title
    public List<Movie> getAllMovies() {
        return movieRepository.findAll().stream()
                .sorted(Comparator.comparing(m -> m.getMovieTitle().toLowerCase()))
                .collect(Collectors.toList());
    }

    // Finds a movie by its ID, throwing an exception if not found
    public Movie findMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie with id " + movieId + " does not exist"));
    }


    // Finds movies by genre name, throwing an exception if none exist
    public List<Movie> findMoviesByGenre(String genreName) {
        Genre genre = genreRepository.findByGenreName(genreName).orElseThrow(() -> new ResourceNotFoundException(
                "Genre with name '" + genreName + "' does not exists"
        ));
        Set<Movie> movieSet = genre.getMovieSet();
        if (movieSet.isEmpty()) {
            throw new ResourceNotFoundException("No movies related to genre with genre '" + genre.getGenreName() + "'");
        }
        return movieRepository.findByGenreSet_genreNameIgnoreCase(genreName);
    }


    // Finds movies by their release year, throwing an exception if none exist
    public List<Movie> findMoviesByReleaseYear(Long releaseYear) {
        boolean yearExits = movieRepository.existsByReleaseYear(releaseYear);
        if (!yearExits) {
            throw new ResourceNotFoundException(
                    "No movie with " + releaseYear + " year");
        }
        return movieRepository.findByReleaseYear(releaseYear);
    }

    // Retrieves actors associated with a specific movie, throwing an exception if none exist
    public Set<Actor> findActorsByMovie(Long movieId) {
        boolean idExists = movieRepository.existsById(movieId);
        if (!idExists) {
            throw new ResourceNotFoundException("Movie with id " + movieId + " does not exists");
        }
        Optional<Movie> movie = movieRepository.findById(movieId);
        Set<Actor> actorSet = movie.get().getActorSet();
        if (actorSet.isEmpty()) {
            throw new ResourceNotFoundException("No actors in movie ID: " + movieId + " title: " + movie.get().getMovieTitle());
        }
        return actorSet;
    }

    // Updates a movie's details, handling title, release year, duration, genres, and actors
    public Movie updateMovie(Long movieId, String movieTitle, Integer releaseYear, Float duration,
                             Set<Genre> genreSet, Set<Actor> actorSet, Set<Actor> actorsToRemove, Set<Genre> genresToRemove) throws BadRequestException {
        // Retrieve the existing movie or throw an exception if not found
        Movie movie = movieRepository.findById(movieId).orElseThrow(() ->
                new ResourceNotFoundException("Movie with id " + movieId + " does not exist"));
        // Check for duplicate movie title (excluding the current movie)
        List<Movie> movies = movieRepository.findByMovieTitleAndMovieIdNot(movieTitle, movieId);
        if (!movies.isEmpty()) {
            throw new BadRequestException("Movie with name '" + movieTitle + "' already exists");
        }
        boolean isUpdated = false;// Flag to track if any updates were made
        // Update movie title if provided and different from existing
        if (movieTitle != null && movieTitle.length() > 0 && !Objects.equals(movie.getMovieTitle(), movieTitle)) {
            movie.setMovieTitle(movieTitle);
            isUpdated = true;
        }
        // Update release year
        if (releaseYear != null && String.valueOf(releaseYear).matches("\\d{4}") && !Objects.equals(movie.getReleaseYear(), releaseYear)) {
            movie.setReleaseYear(releaseYear);
            isUpdated = true;
        }
        // Update duration
        if (duration != null && duration > 0 && !Objects.equals(movie.getDuration(), duration)) {
            movie.setDuration(duration);
            isUpdated = true;
        }
        // Update genres
        isUpdated = addGenres(genreSet, movie, isUpdated);

        // Update actors
        isUpdated = addActors(actorSet, movie, isUpdated);

        // Remove actors
        isUpdated = removeActors(actorsToRemove, movie, isUpdated);
        // Remove genres
        isUpdated = removeGenres(genresToRemove, movie, isUpdated);

        // Save the updated movie if any changes were made
        if (isUpdated) {
            movieRepository.save(movie);
        }
        return movie;

    }

    // Removes specified genres from the movie's genre set, returning true if updated
    private static boolean removeGenres(Set<Genre> genresToRemove, Movie movie, boolean isUpdated) {
        if (genresToRemove != null && !genresToRemove.isEmpty()) {
            Set<Genre> existingGenres = movie.getGenreSet();
            for (Genre genreToRemove : genresToRemove) {
                existingGenres.removeIf(existingGenre ->
                        Objects.equals(existingGenre.getGenreId(), genreToRemove.getGenreId()) ||
                                (existingGenre.getGenreName() != null && existingGenre.getGenreName().equals(genreToRemove.getGenreName())));
            }
            movie.setGenreSet(existingGenres);
            isUpdated = true;
        }
        return isUpdated;
    }

    // Removes specified actors from the movie's actor set, returning true if updated
    private static boolean removeActors(Set<Actor> actorsToRemove, Movie movie, boolean isUpdated) {
        if (actorsToRemove != null && !actorsToRemove.isEmpty()) {
            Set<Actor> existingActors = movie.getActorSet();
            // Remove genres that match either by ID or name
            for (Actor actorToRemove : actorsToRemove) {
                existingActors.removeIf(existingActor ->
                        Objects.equals(existingActor.getActorId(), actorToRemove.getActorId()) ||
                                (existingActor.getActorName() != null && existingActor.getActorName().equals(actorToRemove.getActorName())));
            }
            movie.setActorSet(existingActors);
            isUpdated = true;
        }
        return isUpdated;
    }

    // Adds specified actors to the movie's actor set, ensuring they exist in the repository
    private boolean addActors(Set<Actor> actorSet, Movie movie, boolean isUpdated) {
        if (actorSet != null && !actorSet.isEmpty()) {
            Set<Actor> existingActors = movie.getActorSet();
            // Remove actors that match either by ID or name
            for (Actor actor : actorSet) {
                Actor existingActor = null;
                // Check by ID if provided
                if (actor.getActorId() != null) {
                    Optional<Actor> actorOptional = actorRepository.findById(actor.getActorId());
                    if (actorOptional.isPresent()) {
                        existingActor = actorOptional.get();
                    } else {
                        throw new ResourceNotFoundException("Actor with ID '" + actor.getActorId() + "' does not exist");
                    }
                }
                // If not found by ID, check by name
                if (existingActor == null && actor.getActorName() != null && !actor.getActorName().isEmpty()) {
                    Optional<Actor> actorByNameOptional = actorRepository.findByActorName(actor.getActorName());
                    if (actorByNameOptional.isPresent()) {
                        existingActor = actorByNameOptional.get();
                    } else {
                        existingActor = actorRepository.save(actor);
                        // throw new ResourceNotFoundException("Actor with name '" + actor.getActorName() + "' does not exist");
                    }
                }
                // If actor is found, add to the set
                if (existingActor != null) {
                    existingActors.add(existingActor);
                }
            }
            movie.setActorSet(existingActors);
            isUpdated = true;
        }
        return isUpdated;
    }

    // Adds specified genres to the movie's genreSet, ensuring they exist in the repository
    private boolean addGenres(Set<Genre> genreSet, Movie movie, boolean isUpdated) {
        if (genreSet != null && !genreSet.isEmpty()) {
            Set<Genre> existingGenres = movie.getGenreSet();
            for (Genre genre : genreSet) {
                Genre existingGenre = null;
                //Check by ID if provided
                if (genre.getGenreId() != null) {
                    Optional<Genre> genreOptional = genreRepository.findById(genre.getGenreId());
                    if (genreOptional.isPresent()) {
                        existingGenre = genreOptional.get();
                    } else {
                        throw new ResourceNotFoundException("Genre with ID " + genre.getGenreId() + " does not exist");
                    }
                }
                // If not found by ID, check by name
                if (existingGenre == null && genre.getGenreName() != null && !genre.getGenreName().isEmpty()) {
                    Optional<Genre> genreByNameOptional = genreRepository.findByGenreNameIgnoreCase(genre.getGenreName());
                    if (genreByNameOptional.isPresent()) {
                        existingGenre = genreByNameOptional.get();
                    } else {
                        // Create and save a new genre if not found
                        existingGenre = new Genre();
                        existingGenre.setGenreName(genre.getGenreName());
                        genreRepository.save(existingGenre);
                    }
                }
                // Create and save a new genre if not found
                if (existingGenre != null) {
                    existingGenres.add(existingGenre);
                }
            }
            movie.setGenreSet(existingGenres);
            isUpdated = true;
        }
        return isUpdated;
    }

    // Retrieves movies associated with a specific genre ID, throwing an exception if none exist
    public Set<Movie> getMoviesByGenreId(Long genreId) {
        Genre genre = genreRepository.findById(genreId).orElseThrow(() -> new ResourceNotFoundException(
                "Genre with ID " + genreId + " does not exists"
        ));
        Set<Movie> movieSet = genre.getMovieSet();
        if (movieSet.isEmpty()) {
            throw new ResourceNotFoundException("No movies related to genre with genre '" + genre.getGenreName() + "'");
        }
        return movieRepository.findMoviesByGenreId(genreId);
    }

    // Deletes a movie by its ID, checking for associated actors and genres unless forced
    public void deleteMovie(Long movieId, boolean force) throws BadRequestException {
        boolean exists = movieRepository.existsById(movieId);
        if (!exists) {
            throw new ResourceNotFoundException("Movie with id '" + movieId + "' does not exist");
        }
        // Count associated actors and genres
        int associatedActorCount = actorRepository.countActorsByMovieId(movieId);
        int associatedGenreCount = genreRepository.countGenresByMovieId(movieId);
        // Prevent deletion if there are associated actors or genres unless forced
        if ((associatedActorCount > 0 || associatedGenreCount > 0) && !force) {
            throw new BadRequestException("Cannot delete movie "
                    + movieRepository.findById(movieId).get().getMovieTitle() +
                    " becasue it has " + associatedActorCount + " associated actors and " +
                    associatedGenreCount + " associated genres");
        } else {
            // Remove associations and delete the movie
            removeActors(movieId);
            removeGenres(movieId);
            movieRepository.deleteById(movieId);
        }
    }

    // Removes genres associated with a movie from their respective sets
    private void removeGenres(Long movieId) {
        Set<Genre> genres = genreRepository.findGenresByMovieId(movieId);
        for (Genre genre : genres) {
            // Remove the movie from the genre's movie set
            genre.getMovieSet().removeIf(movie -> movie.getMovieId().equals(movieId));
        }
    }

    // Removes actors associated with a movie from their respective sets
    private void removeActors(Long movieId) {
        Set<Actor> actors = actorRepository.findActorsByMovieId(movieId);
        // Remove the movie from the actor's movie set
        for (Actor actor : actors) {
            actor.getMovieSet().removeIf(movie -> movie.getMovieId().equals(movieId));
        }
    }

    // Finds movies by a partial name and throws an exception if none found
    public Set<Movie> findMoviesByPartialName(String someName) {
        System.out.println("Searching for movies with name: " + someName);
        Set<Movie> movieSet = movieRepository.findByPartialMovieTitle(someName);
        if (movieSet.isEmpty()) {
            throw new ResourceNotFoundException("No movies with name: " + someName);
        }
        return new HashSet<>(movieSet);
    }

    public Page<Movie> getMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }
}
