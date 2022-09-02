package com.reactive.controller;

import com.reactive.dao.model.MovieInfo;
import com.reactive.service.MovieInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MovieInfoController {

    private final MovieInfoService movieInfoService;

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService.addMovieInfo(movieInfo);
    }

    @GetMapping("/movieinfos")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year) {

        Flux<MovieInfo> movieInfoFlux = null;
        if (year != null) {
            return  movieInfoService.getMovieByYear(year);
        }
        return movieInfoService.getAllMovieInfos();
    }

    @GetMapping("/movieinfos/{movieInfoId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String movieInfoId) {
        return movieInfoService.findMovieInfoById(movieInfoId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @PutMapping("/movieinfos/{movieInfoId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfoById(@PathVariable String movieInfoId, @RequestBody @Valid MovieInfo updatedMovieInfo) {
        return movieInfoService.updateMovieInfoById(movieInfoId, updatedMovieInfo)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/movieinfos/{movieInfoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfoById(@PathVariable String movieInfoId) {
        return movieInfoService.deleteMovieById(movieInfoId);
    }
}
