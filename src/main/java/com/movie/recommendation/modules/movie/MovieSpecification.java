package com.movie.recommendation.modules.movie;

import com.movie.recommendation.modules.movie.entity.Movie;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class MovieSpecification {

    private MovieSpecification() {
    }

    public static Specification<Movie> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<Movie> hasGenre(Integer genreId) {
        if (genreId == null) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("genres", JoinType.INNER).get("id"), genreId);
        };
    }

    public static Specification<Movie> releasedInYear(Integer year) {
        if (year == null) return null;
        return (root, query, cb) ->
                cb.equal(
                        cb.function("date_part", Double.class,
                                cb.literal("year"), root.get("releaseDate")).as(Integer.class),
                        year);
    }

    public static Specification<Movie> hasMinRating(BigDecimal minRating) {
        if (minRating == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("avgRating"), minRating);
    }
}
