package com.reactive.dao.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class MovieInfo {

    @Id
    private String movieInfoId;

    @NotBlank(message = "Movie name must not be empty")
    private String name;

    @NotNull(message = "Movie year can't be empty")
    @Positive(message = "Movie year must be positive")
    private Integer year;

    @NotEmpty(message = "Movie cast can't be empty")
    private List<@NotBlank(message = "Cast name can't be blank") String> cast;
    private LocalDate releaseDate;
}
