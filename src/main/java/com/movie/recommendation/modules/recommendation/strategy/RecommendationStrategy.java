package com.movie.recommendation.modules.recommendation.strategy;

import com.movie.recommendation.modules.recommendation.entity.Recommendation;

import java.util.List;

public interface RecommendationStrategy {

    List<Recommendation> recommend(Long userId, int limit);

    Recommendation.StrategyType getStrategyType();
}
