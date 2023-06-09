package com.uade.ad.controller;

import com.uade.ad.controller.dto.ReviewCreateDto;
import com.uade.ad.model.Genre;
import com.uade.ad.model.Movie;
import com.uade.ad.model.Review;
import com.uade.ad.model.User;
import com.uade.ad.service.MovieService;
import com.uade.ad.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    private final UserService userService;

    public MovieController(MovieService movieService, UserService userService) {
        this.movieService = movieService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getMovies(@RequestParam Optional<Long> cinema,
                                       @RequestParam Optional<Double> latitude, @RequestParam Optional<Double> longitude,
                                       @RequestParam Optional<Double> distance,
                                       @RequestParam Optional<String> title,
                                       @RequestParam Optional<String> genre,
                                       @RequestParam Optional<Double> rating) {
        try {
            List<Movie> movies = movieService.getMoviesBy(cinema, latitude, longitude, distance, title, genre, rating);
            if (movies.isEmpty()) return new ResponseEntity<>("Movies.", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(movies, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving movies: " + e.getMessage());
        }
    }

    @PostMapping("/{movieId}/review")
    public ResponseEntity<?> createUser(@PathVariable Integer movieId, @RequestBody @Valid ReviewCreateDto reviewCreateDto) {
        try {
            Optional<User> user = userService.findById(Long.valueOf(reviewCreateDto.getUserId()));

            if (user.isPresent()) {
                Review review = movieService.createReview(movieId, reviewCreateDto, user.get());
                if (Objects.isNull(review)) {return new ResponseEntity<>("Movie not found", HttpStatus.NOT_FOUND);}
                return new ResponseEntity<>(review, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user: " + e.getMessage());
        }
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<?> getMovieById(@PathVariable Integer movieId) {
        try {
            Optional<Movie> movie = movieService.findById(movieId);
            if (movie.isEmpty()) return new ResponseEntity<>("Movie not found.", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(movie.get(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error finding user: " + e.getMessage());
        }
    }

    @GetMapping("/genres")
    public ResponseEntity<?> getGenres() {
        try {
            List<Genre> genres = movieService.getGenres();
            return new ResponseEntity<>(genres, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving genres: " + e.getMessage());
        }
    }
}
