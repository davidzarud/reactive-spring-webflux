package com.reactive.test.unit;

import com.reactive.controller.FluxAndMonoController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AutoConfigureWebTestClient
@WebFluxTest(FluxAndMonoController.class)
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void flux() {

        webTestClient.get()
                .uri(URI.create("/flux"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Integer.class)
                .hasSize(7);
    }

    @Test
    void flux_content() {

        var flux = webTestClient.get()
                .uri(URI.create("/flux"))
                .exchange()
                .expectStatus().isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7)
                .verifyComplete();
    }

    @Test
    void flux_content_v2() {

        Flux<Integer> flux = webTestClient.get()
                .uri(URI.create("/flux"))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    assertThat(Objects.requireNonNull(responseBody).size()).isEqualTo(7);
                });

    }

    @Test
    void mono() {

        webTestClient.get()
                .uri(URI.create("/mono"))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isEqualTo("Hello World Mono");
                });
    }

    @Test
    void stream() {

        Flux<Long> flux = webTestClient.get()
                .uri(URI.create("/stream"))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()
                .verify();
    }
}