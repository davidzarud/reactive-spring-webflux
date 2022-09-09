package com.reactive.exception;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDataException extends RuntimeException {

    private String message;
}
