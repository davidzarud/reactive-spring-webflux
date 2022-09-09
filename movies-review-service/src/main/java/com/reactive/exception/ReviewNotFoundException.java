package com.reactive.exception;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewNotFoundException extends RuntimeException {

    private String message;
    private Throwable ex;
}
