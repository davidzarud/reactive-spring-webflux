package com.reactive.test.integration;

import com.reactive.dao.model.MovieInfo;
import com.reactive.dao.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MovieInfoControllerIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    List<MovieInfo> movieInfoList;
    static final String MOVIE_INFOS_URI = "/v1/movieinfos";

    @BeforeEach
    void setUp() {

        movieInfoList = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        movieInfoRepository.saveAll(movieInfoList).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }
    @Test
    void addMovieInfo() throws InterruptedException {

        MovieInfo movieInfo = MovieInfo.builder()
                .name("Day Shift")
                .year(2022)
                .cast(List.of("Jamie Foxx"))
                .releaseDate(LocalDate.of(2022, Month.AUGUST, 1))
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        webTestClient.post()
                .uri(MOVIE_INFOS_URI)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody.getName()).isEqualTo(movieInfo.getName());
                    assertThat(responseBody.getReleaseDate()).isEqualTo(movieInfo.getReleaseDate());
                    assertThat(responseBody.getCast()).isEqualTo(movieInfo.getCast());
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void getAllMovies() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        webTestClient.get()
                .uri(MOVIE_INFOS_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MovieInfo.class)
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(!CollectionUtils.isEmpty(responseBody)).isTrue();
                    assertThat(responseBody.size()).isEqualTo(3);
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
}