package com.reactive.handler;

import com.reactive.dao.model.Review;
import com.reactive.dao.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ReviewHandler {

    private final ReviewRepository reviewRepository;

    public Mono<ServerResponse> addNewReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .flatMap(reviewRepository::save)
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview));
    }

    public Mono<ServerResponse> getAllReviews(ServerRequest request) {

        return request.queryParam("movieInfoId")
                .map(s -> ServerResponse.ok().body(reviewRepository.findByMovieInfoId(Long.valueOf(s)), Review.class))
                .orElseGet(() -> ServerResponse.ok().body(reviewRepository.findAll(), Review.class));
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        return reviewRepository.findById(request.pathVariable("id"))
                .flatMap(review -> request.bodyToMono(Review.class)
                .map(updatedReview -> {

                    review.setComment(updatedReview.getComment());
                    review.setRating(updatedReview.getRating());
                    return review;
                }))
                .flatMap(reviewRepository::save)
                .flatMap(review -> ServerResponse.ok().bodyValue(review))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {

        String reviewId = request.pathVariable("id");

        return reviewRepository.findById(reviewId)
                .flatMap(review -> reviewRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        return reviewRepository.findById(request.pathVariable("id"))
                .flatMap(review -> ServerResponse.ok().bodyValue(review))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
