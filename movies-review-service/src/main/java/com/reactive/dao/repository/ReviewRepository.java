package com.reactive.dao.repository;

import com.reactive.dao.model.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReviewRepository extends ReactiveMongoRepository<Review, String> {

    Flux<Review> findByMovieInfoId(Long movieInfoId);
}
