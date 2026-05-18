package com.movie.recommendation.modules.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreResponse {

    private Integer id;
    private String name;
    private String slug;
}
