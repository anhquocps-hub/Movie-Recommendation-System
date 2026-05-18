package com.movie.recommendation.modules.watchlist;

import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.movie.MovieRepository;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.User;
import com.movie.recommendation.modules.watchlist.dto.WatchlistResponse;
import com.movie.recommendation.modules.watchlist.entity.WatchlistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public PagedResponse<WatchlistResponse> getUserWatchlist(Long userId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt").descending());
        Page<WatchlistResponse> items = watchlistRepository.findAllByUserId(userId, pageable)
                .map(this::toResponse);
        return PagedResponse.from(items);
    }

    @Transactional
    public WatchlistResponse addToWatchlist(Long userId, Long movieId) {
        Movie movie = movieRepository.findByIdAndIsActiveTrue(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (watchlistRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new DuplicateResourceException("Watchlist", "movieId", movieId);
        }

        WatchlistItem item = WatchlistItem.builder()
                .user(user)
                .movie(movie)
                .build();

        return toResponse(watchlistRepository.save(item));
    }

    @Transactional
    public void removeFromWatchlist(Long userId, Long movieId) {
        WatchlistItem item = watchlistRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist item", "movieId", movieId));
        watchlistRepository.delete(item);
    }

    private WatchlistResponse toResponse(WatchlistItem item) {
        Movie movie = item.getMovie();
        return WatchlistResponse.builder()
                .id(item.getId())
                .movieId(movie.getId())
                .movieTitle(movie.getTitle())
                .movieSlug(movie.getSlug())
                .posterUrl(movie.getPosterUrl())
                .avgRating(movie.getAvgRating())
                .addedAt(item.getAddedAt())
                .build();
    }
}
