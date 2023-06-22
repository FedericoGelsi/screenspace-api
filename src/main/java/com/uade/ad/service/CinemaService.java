package com.uade.ad.service;

import com.google.maps.model.LatLng;
import com.uade.ad.controller.dto.CinemaCreateDto;
import com.uade.ad.controller.dto.CinemaUpdateDto;
import com.uade.ad.controller.dto.HallCreateDto;
import com.uade.ad.controller.dto.ShowCreateDto;
import com.uade.ad.model.Cinema;
import com.uade.ad.model.Hall;
import com.uade.ad.model.CinemaShow;
import com.uade.ad.model.Movie;
import com.uade.ad.repository.CinemaRepository;
import com.uade.ad.repository.HallRepository;
import com.uade.ad.repository.MovieRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;

    private final MovieRepository movieRepository;

    private final MapService mapService;

    @Autowired
    public CinemaService(CinemaRepository cinemaRepository, MapService mapService, HallRepository hallRepository, MovieRepository movieRepository) {

        this.cinemaRepository = cinemaRepository;
        this.mapService = mapService;
        this.hallRepository = hallRepository;
        this.movieRepository = movieRepository;
    }

    public List<Cinema> getAll(Integer movieId, Long ownerId) {
        if (ownerId != null) {
            return cinemaRepository.findAllByOwnerId(ownerId);
        }

        if (movieId != null) {
            List<Cinema> cinemas = cinemaRepository.findAll();
            return cinemas.stream()
                    .filter(cinema -> cinema.getHalls().stream()
                            .flatMap(hall -> hall.getCinemaShows().stream())
                            .anyMatch(cinemaShow -> Objects.equals(cinemaShow.getMovie().getId(), movieId)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Optional<Cinema> findById(Long id) {
        return cinemaRepository.findById(id);
    }

    public Cinema updateCinema(CinemaUpdateDto cinemaDTO, Cinema existingCinema) {
        LatLng coordinates = mapService.getLocationFromAddress(cinemaDTO.getAddress(), cinemaDTO.getPostalCode(), cinemaDTO.getCity(),
                cinemaDTO.getProvince(), cinemaDTO.getCountry());
        BeanUtils.copyProperties(cinemaDTO, existingCinema, "id");
        existingCinema.setLatitude(coordinates.lat);
        existingCinema.setLongitude(coordinates.lng);
        return cinemaRepository.save(existingCinema);
    }

    public boolean deleteCinemaById(Long id) {
        Optional<Cinema> cinema = cinemaRepository.findById(id);
        if (cinema.isPresent()) {
            cinemaRepository.delete(cinema.get());
            return true;
        }
        return false;
    }

    public Cinema createCinema(CinemaCreateDto cinemaDto) {
        LatLng coordinates = mapService.getLocationFromAddress(cinemaDto.getAddress(), cinemaDto.getPostalCode(), cinemaDto.getCity(),
                cinemaDto.getProvince(), cinemaDto.getCountry());
        Cinema newCinema = Cinema
                .builder()
                .ownerId(cinemaDto.getUserId())
                .cinemaName(cinemaDto.getCinemaName())
                .companyName(cinemaDto.getCompanyName())
                .address(cinemaDto.getAddress())
                .postalCode(cinemaDto.getPostalCode())
                .city(cinemaDto.getCity())
                .province(cinemaDto.getProvince())
                .country(cinemaDto.getCountry())
                .latitude(coordinates.lat)
                .longitude(coordinates.lng)
                .pricePerShow(cinemaDto.getPricePerShow())
                .active(cinemaDto.isActive())
                .build();
        return cinemaRepository.save(newCinema);
    }

    public Hall createHall(Long cinemaId, HallCreateDto hallDto) throws Exception {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new Exception("Cinema not found."));

        Hall newHall = Hall
                .builder()
                .name(hallDto.getName())
                .width(hallDto.getWidth())
                .height(hallDto.getHeight())
                .available(hallDto.isAvailable())
                .cinema(cinema)
                .build();

        hallRepository.save(newHall);

        cinema.getHalls().add(newHall);
        cinemaRepository.save(cinema);

        return newHall;
    }

    public Hall updateHall(Long cinemaId, Long hallId, HallCreateDto hallDto) throws Exception {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new Exception("Cinema not found."));

        Hall hallToUpdate = cinema.getHalls().stream()
                .filter(x -> Objects.equals(x.getId(), hallId)).findFirst()
                .orElseThrow(() -> new Exception("Hall not found."));

        BeanUtils.copyProperties(hallDto, hallToUpdate, "id");
        cinemaRepository.save(cinema);
        return hallToUpdate;
    }

    public boolean deleteHall(Long cinemaId, Long hallId) throws Exception {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new Exception("Cinema not found."));

        boolean isDeleted = cinema.getHalls().removeIf(hall -> Objects.equals(hall.getId(), hallId));
        cinemaRepository.save(cinema);

        return isDeleted;
    }

    public CinemaShow createShow(Long cinemaId, Long hallId, ShowCreateDto showDto) throws Exception {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new Exception("Cinema not found."));

        Hall hall = cinema.getHalls().stream()
                .filter(x -> Objects.equals(x.getId(), hallId)).findFirst()
                .orElseThrow(() -> new Exception("Hall not found."));

        Optional<Movie> movie = movieRepository.findById(showDto.getMovieId().intValue());

        if (movie.isEmpty()) throw new Exception("Movie not found.");

        CinemaShow newCinemaShow = CinemaShow.builder()
                .movie(movie.get())
                .name(showDto.getName())
                .datetime(showDto.getDatetime())
                .build();
        newCinemaShow.initSeats(hall.getHeight(),hall.getWidth());

        hall.getCinemaShows().add(newCinemaShow);
        cinemaRepository.save(cinema);
        return newCinemaShow;
    }

    public CinemaShow updateCinemaShow(Long cinemaId, Long hallId, Long showId, ShowCreateDto showDto) throws Exception {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new Exception("Cinema not found."));

        CinemaShow showToUpdate = cinema.getHalls().stream()
                .filter(x -> Objects.equals(x.getId(), hallId)).findFirst()
                .orElseThrow(() -> new Exception("Hall not found."))
                .getCinemaShows().stream()
                .filter(x -> Objects.equals(x.getId(), showId)).findFirst()
                .orElseThrow(() -> new Exception("Show not found"));

        BeanUtils.copyProperties(showDto, showToUpdate, "id");
        cinemaRepository.save(cinema);
        return showToUpdate;
    }

    public boolean deleteShow(Long cinemaId, Long hallId, Long showId) throws Exception {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new Exception("Cinema not found."));
        boolean isDeleted = false;
        try {
            cinema.getHalls().forEach(
                    hall -> {
                        hall.getCinemaShows().removeIf(show -> Objects.equals(show.getId(), showId));
                    }
            );
            cinemaRepository.save(cinema);
            isDeleted = true;
        } catch (Exception e) {
            throw new Exception("Show not deleted");
        }

        return isDeleted;

    }
}