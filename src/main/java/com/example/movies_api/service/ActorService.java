package com.example.movies_api.service;

import com.example.movies_api.entities.Actor;
import com.example.movies_api.entities.Movie;
import com.example.movies_api.exceptions.ResourceNotFoundException;
import com.example.movies_api.repository.ActorRepository;
import com.example.movies_api.repository.MovieRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActorService {
    @Autowired
    ActorRepository actorRepository;
    @Autowired
    MovieRepository movieRepository;


    public List<Actor> getAllActors() {
        List<Actor> actorList = actorRepository.findAll();
        return actorList.stream().sorted((actor1, actor2) -> actor1.getActorName().compareTo(actor2.getActorName()))
                .collect(Collectors.toList());
    }


    public Actor save(Actor actor) throws BadRequestException {
        Optional<Actor> actorCheck = actorRepository.findByActorName(actor.getActorName());
        if (actorCheck.isPresent()) {
            throw new BadRequestException("Actor " + actor.getActorName() + " already exist");
        }
        return actorRepository.save(actor);
    }


    public Actor findActorById(Long actorId) {
        boolean exists = actorRepository.existsById(actorId);
        if (!exists) {
            throw new ResourceNotFoundException("Actor with id " + actorId + " does not exists");
        }
        return actorRepository.findById(actorId).orElseThrow();
    }

    public Set<Movie> getMoviesByActorId(Long actorId) {
        boolean exists = actorRepository.existsById(actorId);
        if (!exists) {
            throw new ResourceNotFoundException("Actor with id " + actorId + " does not exists");
        }
        Set<Movie> movieSet = actorRepository.findMoviesByActorId(actorId);
        if (movieSet.isEmpty()) {
            throw new ResourceNotFoundException("No movies found fot the actor with ID " + actorId);
        }
        return movieSet;
    }

    public void updateActor(Long actorId, String actorName, LocalDate birthDate, Set<Movie> movieSet) throws BadRequestException {
        Actor actor = actorRepository.findById(actorId).orElseThrow(() -> new ResourceNotFoundException(
                "Actor with id " + actorId + " does not exists"
        ));
        if (actorName != null && actorName.length() > 0 && !Objects.equals(actor.getActorName(), actorName)) {
            actor.setActorName(actorName);

        }
        if (birthDate != null && !actor.getBirthDate().equals(birthDate)) {
            actor.setBirthDate(birthDate);
        }
        if (movieSet != null && !movieSet.isEmpty()) {
            actor.setMovieSet(movieSet);
        }
        actorRepository.save(actor);
    }

    @Transactional
    public void deleteActor(Long actorId, boolean force) throws BadRequestException {
        boolean exists = actorRepository.existsById(actorId);
        if (!exists) {
            throw new ResourceNotFoundException("Actor with id " + actorId + " does not exits");
        }
        int associatedMoviesCount = movieRepository.countMoviesByActorId(actorId);

        if (associatedMoviesCount > 0 && !force) {
            throw new BadRequestException("Cannot delete genre " + actorRepository.findById(actorId).get().getActorName() +
                    " because it has " + associatedMoviesCount + " associated movies.");
        } else {
            // Remove actor from all associated movies
            Set<Movie> movies = actorRepository.findMoviesByActorId(actorId);
            for (Movie movie : movies) {
                movie.getActorSet().removeIf(actor -> actor.getActorId().equals(actorId));
            }
            actorRepository.deleteById(actorId);
        }
    }


    public Set<Actor> findActorsByPartialName(String someName) {
        Set<Actor> actors = actorRepository.findByPartialActorName(someName);
        if (actors.isEmpty()) {
            throw new ResourceNotFoundException("No actors found with name containing: " + someName);
        }
        return new HashSet<>(actors);
    }
}