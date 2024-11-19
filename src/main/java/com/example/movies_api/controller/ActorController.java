package com.example.movies_api.controller;

import com.example.movies_api.entities.Actor;
import com.example.movies_api.entities.Movie;
import com.example.movies_api.service.ActorService;
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
@RequestMapping("/api/actor")
public class ActorController {
    @Autowired
    private ActorService actorService;

    @GetMapping
    public List<Actor> getAllActors() {
        return actorService.getAllActors();
    }

    @PostMapping("/add-actor")
    public ResponseEntity<Actor> addActor(@Valid @RequestBody Actor actor) throws BadRequestException {
        return new ResponseEntity<Actor>(actorService.save(actor), HttpStatus.CREATED);
    }

    @GetMapping("{actorId}")
    public ResponseEntity<Actor> getActorById(@PathVariable("actorId") Long actorId) {
        return new ResponseEntity<Actor>(actorService.findActorById(actorId), HttpStatus.OK);
    }

    @GetMapping("/{actorId}/movies")
    public ResponseEntity<Set<Movie>> getMoviesByActor(@PathVariable Long actorId) {
        return new ResponseEntity<Set<Movie>>(actorService.getMoviesByActorId(actorId), HttpStatus.OK);
    }

    @PatchMapping("/update/{actorId}")
    public ResponseEntity<Actor> updateActor(@PathVariable Long actorId, @RequestBody Actor actor) throws BadRequestException {
        actorService.updateActor(actorId, actor.getActorName(), actor.getBirthDate(), actor.getMovieSet());
        return ResponseEntity.ok(actor);
    }

    @DeleteMapping("{actorId}")
    @Transactional
    public ResponseEntity<?> deleteActor(@PathVariable Long actorId, @RequestParam(name = "force", defaultValue = "false") boolean force) {
        try {
            actorService.deleteActor(actorId, force);
            return ResponseEntity.noContent().build();
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<Set<Actor>> findActors(@PathVariable String name) {
        return new ResponseEntity<>(actorService.findActorsByPartialName(name), HttpStatus.OK);
    }
}
