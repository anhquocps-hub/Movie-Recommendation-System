package com.movie.recommendation.modules.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationScheduler {

    private final RecommendationService recommendationService;

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000, initialDelay = 60 * 1000)
    public void generateRecommendations() {
        log.info("Scheduled recommendation generation triggered");
        recommendationService.generateRecommendationsForAllUsers();
    }
}
