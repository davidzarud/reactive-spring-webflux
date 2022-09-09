package com.reactive.handler;

import com.reactive.dao.model.Review;
import com.reactive.dao.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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

        return ServerResponse.ok().body(reviewRepository.findAll(), Review.class);
    }
}
