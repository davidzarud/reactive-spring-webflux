package com.reactive.test.integration;

import com.reactive.dao.model.MovieInfo;
import com.reactive.dao.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    List<MovieInfo> movieInfoList;

    @BeforeEach
    void setUp() {

        movieInfoList = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "Bla Bla",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        // Block last makes sure saveAll() method is done executing before moving to the test cases
        movieInfoRepository.saveAll(movieInfoList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {

        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() throws InterruptedException {

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc").log();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        movieInfoMono.subscribe(movieInfo -> {
            assertThat(movieInfo.getName()).isEqualTo(movieInfoList.get(2).getName());
            assertThat(movieInfo.getCast()).isEqualTo(movieInfoList.get(2).getCast());
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void findById_v2() throws InterruptedException {

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc").log();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertThat(movieInfo.getName()).isEqualTo(movieInfoList.get(2).getName());
                    assertThat(movieInfo.getCast()).isEqualTo(movieInfoList.get(2).getCast());
                    countDownLatch.countDown();
                }).verifyComplete();

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void findById_v3() throws InterruptedException {

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc").log();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        StepVerifier.create(movieInfoMono)
                .consumeNextWith(movieInfo -> {
                    assertThat(movieInfo.getName()).isEqualTo(movieInfoList.get(2).getName());
                    assertThat(movieInfo.getCast()).isEqualTo(movieInfoList.get(2).getCast());
                    countDownLatch.countDown();
                }).verifyComplete();

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void saveMovieInfo() {

        MovieInfo movieInfo = new MovieInfo(null, "Snatch", 2000, List.of("Jason Statham", "Vinnie Jones"), LocalDate.parse("2000-02-11"));

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(persistedMovieInfo -> {
                    assertThat(persistedMovieInfo.getMovieInfoId()).isNotBlank();
                    assertThat(persistedMovieInfo.getName()).isEqualTo("Snatch");
                }).verifyComplete();

        StepVerifier.create(movieInfoRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc").log();
        MovieInfo movieInfo = movieInfoMono.block();
        movieInfo.setName("The Dark Knight Rises");

        Mono<MovieInfo> updatedMovieMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(updatedMovieMono)
                .assertNext(updatedMovieInfo -> {
                    assertThat(updatedMovieInfo).isNotNull();
                    assertThat(updatedMovieInfo.getName()).isEqualTo("The Dark Knight Rises");
                    countDownLatch.countDown();;
                }).verifyComplete();

        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void deleteMovieInfo() {

        movieInfoRepository.deleteById("abc").log().block();
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findAllByYear() {

        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(movieInfoFlux)
                .consumeNextWith(movieInfo -> {
                    assertThat(movieInfo).isNotNull();
                    assertThat(movieInfo.getYear()).isEqualTo(2005);
                }).expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);
        String movieName = "Dark Knight Rises";
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findByName(movieName);

        StepVerifier.create(movieInfoMono)
                .consumeNextWith(movieInfo -> {
                    assertThat(movieInfo).isNotNull();
                    assertThat(movieInfo.getName()).isEqualTo(movieName);
                    countDownLatch.countDown();
                }).verifyComplete();

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);

    }

}