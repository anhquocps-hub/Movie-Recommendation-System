package com.movie.recommendation.modules.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {

    @NotNull(message = "Preferences list must not be null")
    private List<String> preferences;
}
