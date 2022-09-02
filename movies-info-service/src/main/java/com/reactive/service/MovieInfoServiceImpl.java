package com.reactive.service;

import com.reactive.dao.model.MovieInfo;
import com.reactive.dao.repository.MovieInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MovieInfoServiceImpl implements MovieInfoService {

    private final MovieInfoRepository movieInfoRepository;

    @Override
    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    @Override
    public Flux<MovieInfo> getAllMovieInfos() {
        return movieInfoRepository.findAll();
    }

    @Override
    public Mono<MovieInfo> findMovieInfoById(String movieInfoId) {
        return movieInfoRepository.findById(movieInfoId);
    }

    @Override
    public Mono<MovieInfo> updateMovieInfoById(String movieInfoId, MovieInfo updatedMovieInfo) {
         return movieInfoRepository.findById(movieInfoId)
                 .flatMap(movieInfo -> {
                     movieInfo.setName(updatedMovieInfo.getName());
                     movieInfo.setYear(updatedMovieInfo.getYear());
                     movieInfo.setReleaseDate(updatedMovieInfo.getReleaseDate());
                     movieInfo.setCast(updatedMovieInfo.getCast());
                     return movieInfoRepository.save(movieInfo);
                 });
    }

    @Override
    public Mono<Void> deleteMovieById(String movieInfoId) {
        return movieInfoRepository.deleteById(movieInfoId);
    }

    @Override
    public Flux<MovieInfo> getMovieByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }
}
