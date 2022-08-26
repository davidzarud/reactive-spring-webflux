package com.reactive.controller;

import com.reactive.dao.model.MovieInfo;
import com.reactive.service.MovieInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MovieInfoController {

    private final MovieInfoService movieInfoService;

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody MovieInfo movieInfo) {
        return movieInfoService.addMovieInfo(movieInfo);
    }

    @GetMapping("/movieinfos")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MovieInfo> getAllMovieInfos() {
        return movieInfoService.getAllMovieInfos();
    }

    @GetMapping("/movieinfos/{movieInfoId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<MovieInfo> getMovieInfoById(@PathVariable String movieInfoId) {
        return movieInfoService.findMovieInfoById(movieInfoId);
    }

    @PutMapping("/movieinfos/{movieInfoId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<MovieInfo> updateMovieInfoById(@PathVariable String movieInfoId, @RequestBody MovieInfo updatedMovieInfo) {
        return movieInfoService.updateMovieInfoById(movieInfoId, updatedMovieInfo);
    }
}
