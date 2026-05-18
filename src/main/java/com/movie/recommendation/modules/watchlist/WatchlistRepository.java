package com.movie.recommendation.modules.watchlist;

import com.movie.recommendation.modules.watchlist.entity.WatchlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {

    Page<WatchlistItem> findAllByUserId(Long userId, Pageable pageable);

    Optional<WatchlistItem> findByUserIdAndMovieId(Long userId, Long movieId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
}
