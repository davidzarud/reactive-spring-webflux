package com.reactive.test.integration;

import com.reactive.dao.model.Review;
import com.reactive.dao.repository.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewRepository reviewRepository;

    private final static String REVIEW_URI = "/v1/reviews";

    @BeforeEach
    void setUp() {

        reviewRepository.saveAll(List.of(
                Review.builder().reviewId("631b19fd61b52a21d88c3b54").movieInfoId(1L).rating(9.0).comment("Amazing").build(),
                Review.builder().reviewId("631b19fd61b52a21d88c3b55").movieInfoId(2L).rating(6.7).comment("Boring").build(),
                Review.builder().reviewId("631b19fd61b52a21d88c3b56").movieInfoId(2L).rating(8.0).comment("Fun").build()))
                .blockLast();

    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll().block();
    }

    @Test
    public void createNewReview() throws InterruptedException {

        Review review = Review.builder().movieInfoId(4L).rating(5.0).comment("BAD!").build();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        webTestClient.post()
                .uri(REVIEW_URI)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review createdReview = reviewEntityExchangeResult.getResponseBody();
                    assertThat(createdReview).isNotNull();
                    assertThat(createdReview.getReviewId()).isNotBlank();
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void getAllReviews() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        webTestClient.get()
                .uri(REVIEW_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .consumeWith(listEntityExchangeResult -> {

                    List<Review> reviews = listEntityExchangeResult.getResponseBody();
                    assertThat(reviews).isNotNull();
                    assertThat(reviews.isEmpty()).isFalse();
                    assertThat(reviews.size()).isEqualTo(3);
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void getAllReviewsByMovieInfoId() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(REVIEW_URI).queryParamIfPresent("movieInfoId", Optional.of(2)).build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .consumeWith(listEntityExchangeResult -> {
                    List<Review> reviewList = listEntityExchangeResult.getResponseBody();
                    assertThat(reviewList).isNotNull();
                    assertThat(reviewList.size()).isEqualTo(2);
                    reviewList.forEach(review -> assertThat(review.getMovieInfoId()).isEqualTo(2));
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void updateReview_notFound() {

        String reviewId = "4";

        webTestClient.put()
                .uri(REVIEW_URI + "/{reviewId}", reviewId)
                .bodyValue(Review.builder()
                        .comment("Movie 1")
                        .rating(3.0)
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateReview() throws InterruptedException {

        String reviewId = "631b19fd61b52a21d88c3b54";
        CountDownLatch countDownLatch = new CountDownLatch(1);

        webTestClient.put()
                .uri(REVIEW_URI + "/{reviewId}", reviewId)
                .bodyValue(Review.builder()
                        .comment("Excellent")
                        .rating(10.0)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {

                    Review updatedReview = reviewEntityExchangeResult.getResponseBody();
                    assertThat(updatedReview).isNotNull();
                    assertThat(updatedReview.getReviewId()).isEqualTo("631b19fd61b52a21d88c3b54");
                    assertThat(updatedReview.getMovieInfoId()).isEqualTo(1L);
                    assertThat(updatedReview.getComment()).isEqualTo("Excellent");
                    assertThat(updatedReview.getRating()).isEqualTo(10.0);
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    public void deleteById_notFound() {

        String reviewId = "5";

        webTestClient.delete()
                .uri(REVIEW_URI + "/{reviewId}", reviewId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void deleteById() {

        String reviewId = "631b19fd61b52a21d88c3b56";

        webTestClient.delete()
                .uri(REVIEW_URI + "/{reviewId}", reviewId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

        assertThat(reviewRepository.findAll().map(review -> review).collect(Collectors.toList()).block()).isNotNull();
        assertThat(reviewRepository.findAll().map(review -> review).collect(Collectors.toList()).block().size()).isEqualTo(2);
    }

    @Test
    public void findById_notFound() {

        String reviewId = "123";

        webTestClient.get()
                .uri(REVIEW_URI + "/{id}", reviewId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void findById() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);
        String reviewId = "631b19fd61b52a21d88c3b56";

        webTestClient.get()
                .uri(REVIEW_URI + "/{id}", reviewId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {

                    Review review = reviewEntityExchangeResult.getResponseBody();
                    assertThat(review).isNotNull();
                    assertThat(review.getReviewId()).isEqualTo("631b19fd61b52a21d88c3b56");
                    assertThat(review.getRating()).isEqualTo(8.0);
                    assertThat(review.getComment()).isEqualTo("Fun");
                    assertThat(review.getMovieInfoId()).isEqualTo(2L);
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
}
