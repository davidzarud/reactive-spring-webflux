package com.reactive.service;

import com.reactive.dao.model.MovieInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieInfoService {

    Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo);
    Flux<MovieInfo> getAllMovieInfos();
}
