package com.reactive.test.unit;

import com.reactive.controller.MovieInfoController;
import com.reactive.dao.model.MovieInfo;
import com.reactive.service.MovieInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@AutoConfigureWebTestClient
@WebFluxTest(MovieInfoController.class)
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoServiceImpl movieInfoServiceMock;

    private List<MovieInfo> movieInfoList;

    static final String MOVIE_INFOS_URI = "/v1/movieinfos";

    @BeforeEach
    void setUp() {
        movieInfoList = List.of(new MovieInfo(null, "Batman Begins",
                        2005,List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
    }

    @Test
    void getAllMoviesInfos(){

        when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfoList));

        webTestClient.get()
                .uri(MOVIE_INFOS_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById(){

        when(movieInfoServiceMock.findMovieInfoById("abc")).thenReturn(Mono.just(movieInfoList.get(2)));

        webTestClient.get()
                .uri(MOVIE_INFOS_URI + "/{id}", "abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertThat(movieInfo).isNotNull();
                    assertThat(movieInfo.getName()).isEqualTo("Dark Knight Rises");
                    assertThat(movieInfo.getMovieInfoId()).isEqualTo("abc");
                });
    }

    @Test
    void addMovieInfo() {

        MovieInfo movieInfo = MovieInfo.builder().movieInfoId(null)
                .name("Dark Knight Rises")
                .year(2005)
                .cast(List.of("Christian Bale", "Michael Cane"))
                .releaseDate(LocalDate.parse("2005-06-15"))
                .build();

        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(MovieInfo.builder().movieInfoId("MockId")
                .name("Dark Knight Rises")
                .year(2005)
                .cast(List.of("Christian Bale", "Michael Cane"))
                .releaseDate(LocalDate.parse("2005-06-15"))
                .build()));

        webTestClient.post()
                .uri(MOVIE_INFOS_URI)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var createdMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertThat(createdMovieInfo).isNotNull();
                    assertThat(createdMovieInfo.getMovieInfoId()).isEqualTo("MockId");
                });
    }

    @Test
    void updateMovieInfo() {

        MovieInfo movieInfo = MovieInfo.builder().movieInfoId(null)
                .name("Dark Knight Rises 2")
                .year(2005)
                .cast(List.of("Christian Bale", "Michael Cane"))
                .releaseDate(LocalDate.parse("2005-06-15"))
                .build();

        when(movieInfoServiceMock.updateMovieInfoById(isA(String.class), isA(MovieInfo.class))).thenReturn(Mono.just(MovieInfo.builder().movieInfoId("abc")
                        .name("Dark Knight Rises 2")
                        .year(2005)
                        .cast(List.of("Christian Bale", "Michael Cane"))
                        .releaseDate(LocalDate.parse("2007-06-15"))
                        .build()));

        webTestClient.put()
                .uri(MOVIE_INFOS_URI + "/{id}", "abc")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertThat(updatedMovieInfo).isNotNull();
                    assertThat(updatedMovieInfo.getMovieInfoId()).isEqualTo("abc");
                    assertThat(updatedMovieInfo.getName()).isEqualTo("Dark Knight Rises 2");
                });
    }

    @Test
    void deleteMovieInfo() {

        String movieId = "abc";

        when(movieInfoServiceMock.deleteMovieById(movieId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(MOVIE_INFOS_URI + "/{id}", "abc")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);
    }

    @Test
    void addMovieFailValidation() {

        MovieInfo movieInfo = MovieInfo.builder()
                .year(2015)
                .cast(Collections.emptyList())
                .releaseDate(LocalDate.parse("2022-01-01"))
                .build();

        webTestClient.post()
                .uri(MOVIE_INFOS_URI)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {

                    String response = stringEntityExchangeResult.getResponseBody();
                    assertThat(response).isNotBlank();
                    assertThat(response).isEqualTo("Movie cast can't be empty, Movie name must not be empty");
                });
    }

    @Test
    void addMovieFailValidation_blankCastName() {

        MovieInfo movieInfo = MovieInfo.builder()
                .year(2015)
                .cast(List.of("David", ""))
                .releaseDate(LocalDate.parse("2022-01-01"))
                .build();

        webTestClient.post()
                .uri(MOVIE_INFOS_URI)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {

                    String response = stringEntityExchangeResult.getResponseBody();
                    assertThat(response).isNotBlank();
                    assertThat(response).isEqualTo("Cast name can't be blank, Movie name must not be empty");
                });
    }
}
