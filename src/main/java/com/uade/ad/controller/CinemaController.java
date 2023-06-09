package com.uade.ad.controller;

import com.uade.ad.controller.dto.CinemaCreateDto;
import com.uade.ad.controller.dto.CinemaUpdateDto;
import com.uade.ad.controller.dto.HallCreateDto;
import com.uade.ad.controller.dto.ShowCreateDto;
import com.uade.ad.model.Cinema;
import com.uade.ad.model.Hall;
import com.uade.ad.model.CinemaShow;
import com.uade.ad.service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    @Autowired
    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCinemas(@RequestParam(required = false) Integer movieId, @RequestParam(required = false) Long ownerID) {
        List<Cinema> cinemas = cinemaService.getAll(movieId, ownerID);
        // if (cinemas.isEmpty()) return new ResponseEntity<>("Cinemas not found.", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(cinemas, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getAllCinemasFilter(
                                                 @RequestParam Optional<Double> latitude, @RequestParam Optional<Double> longitude,
                                                 @RequestParam Optional<Double> distance,
                                                 @RequestParam Optional<String> genre,
                                                 @RequestParam Optional<Double> rating){
        List<Cinema> cinemas = cinemaService.getAllFilter(latitude, longitude, distance, genre, rating);
        return new ResponseEntity<>(cinemas, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<?> updateCinema(@RequestBody CinemaUpdateDto cinemaDTO) {

        Optional<Cinema> existingCinema = cinemaService.findById(cinemaDTO.getId());
        if (existingCinema.isEmpty()) {
            return new ResponseEntity<>("Cinema not found.", HttpStatus.NOT_FOUND);
        }

        Cinema updatedCinema = cinemaService.updateCinema(cinemaDTO, existingCinema.get());
        return new ResponseEntity<>(updatedCinema, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCinemaById(@PathVariable("id") Long id) {
        Optional<Cinema> cinema = cinemaService.findById(id);
        if (cinema.isEmpty()) return new ResponseEntity<>("Cinema not found.", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(cinema.get(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCinemaById(@PathVariable("id") Long id) {
        boolean deleted = cinemaService.deleteCinemaById(id);
        if (!deleted) return new ResponseEntity<>("Cinema not found.", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>("Cinema successfully deleted!", HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<?> createCinema(@RequestBody CinemaCreateDto cinemaDto) {
        try {
            Cinema createdCinema = cinemaService.createCinema(cinemaDto);
            return new ResponseEntity<>(createdCinema.toDto(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating cinema: " + e.getMessage());
        }
    }

    @PostMapping("/{cinemaId}/halls")
    public ResponseEntity<?> createHall(@PathVariable("cinemaId") Long cinemaId, @RequestBody HallCreateDto hallDto) {
        try {
            Hall createdHall = cinemaService.createHall(cinemaId, hallDto);
            return new ResponseEntity<>(createdHall.toDto(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating hall: " + e.getMessage());
        }
    }

    @PutMapping("/{cinemaId}/halls/{hallId}")
    public ResponseEntity<?> updateHall(@PathVariable("cinemaId") Long cinemaId, @PathVariable("hallId") Long hallId, @RequestBody HallCreateDto hallDto) {
        try {
            Hall updateHall = cinemaService.updateHall(cinemaId, hallId, hallDto);
            return new ResponseEntity<>(updateHall.toDto(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating hall: " + e.getMessage());
        }
    }

    @DeleteMapping("/{cinemaId}/halls/{hallId}")
    public ResponseEntity<?> deleteHall(@PathVariable("cinemaId") Long cinemaId, @PathVariable("hallId") Long hallId) {
        try {
            boolean deleted = cinemaService.deleteHall(cinemaId, hallId);
            if (!deleted) return new ResponseEntity<>("Hall not found.", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>("Hall successfully deleted!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting hall: " + e.getMessage());
        }
    }

    @PostMapping("/{cinemaId}/halls/{hallId}/shows")
    public ResponseEntity<?> createShow(@PathVariable("cinemaId") Long cinemaId, @PathVariable("hallId") Long hallId, @RequestBody ShowCreateDto showDto) {
        try {
            CinemaShow createdCinemaShow = cinemaService.createShow(cinemaId,hallId,showDto);
            return new ResponseEntity<>(createdCinemaShow.toDto(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating show: " + e.getMessage());
        }
    }

    @PutMapping("/{cinemaId}/halls/{hallId}/shows/{showId}")
    public ResponseEntity<?> updateShow(@PathVariable("cinemaId") Long cinemaId,
                                        @PathVariable("hallId") Long hallId,
                                        @PathVariable("showId") Long showId,
                                        @RequestBody ShowCreateDto showDto) {
        try {
            CinemaShow updateShow = cinemaService.updateCinemaShow(cinemaId, hallId, showId, showDto);
            return new ResponseEntity<>(updateShow.toDto(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating show: " + e.getMessage());
        }
    }

    @DeleteMapping("/{cinemaId}/halls/{hallId}/shows/{showId}")
    public ResponseEntity<?> deleteShow(@PathVariable("cinemaId") Long cinemaId,
                                        @PathVariable("hallId") Long hallId,
                                        @PathVariable("showId") Long showId) {
        try {
            boolean deleted = cinemaService.deleteShow(cinemaId, hallId, showId);
            if (!deleted) return new ResponseEntity<>("Show not found.", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>("Show successfully deleted!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting show: " + e.getMessage());
        }
    }
}
