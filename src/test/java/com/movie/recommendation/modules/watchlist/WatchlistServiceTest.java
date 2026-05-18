package com.movie.recommendation.modules.watchlist;

import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.movie.MovieRepository;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import com.movie.recommendation.modules.watchlist.dto.WatchlistResponse;
import com.movie.recommendation.modules.watchlist.entity.WatchlistItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock private WatchlistRepository watchlistRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    private User buildUser() {
        return User.builder().id(1L).email("u@test.com").username("user")
                .passwordHash("h").role(Role.USER).isActive(true).build();
    }

    private Movie buildMovie() {
        return Movie.builder().id(1L).title("Test").slug("test")
                .avgRating(BigDecimal.ZERO).voteCount(0).isActive(true)
                .genres(new HashSet<>()).build();
    }

    @Test
    void getUserWatchlist_returnsPaged() {
        Movie movie = buildMovie();
        User user = buildUser();
        WatchlistItem item = WatchlistItem.builder().id(1L).user(user).movie(movie).build();
        Page<WatchlistItem> page = new PageImpl<>(List.of(item));

        when(watchlistRepository.findAllByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        var result = watchlistService.getUserWatchlist(1L, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void addToWatchlist_success() {
        User user = buildUser();
        Movie movie = buildMovie();

        when(movieRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(movie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(watchlistRepository.existsByUserIdAndMovieId(1L, 1L)).thenReturn(false);
        when(watchlistRepository.save(any(WatchlistItem.class))).thenAnswer(inv -> {
            WatchlistItem i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        WatchlistResponse result = watchlistService.addToWatchlist(1L, 1L);

        assertThat(result.getMovieTitle()).isEqualTo("Test");
        verify(watchlistRepository).save(any(WatchlistItem.class));
    }

    @Test
    void addToWatchlist_duplicate_throws() {
        when(movieRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(buildMovie()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser()));
        when(watchlistRepository.existsByUserIdAndMovieId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> watchlistService.addToWatchlist(1L, 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void removeFromWatchlist_success() {
        WatchlistItem item = WatchlistItem.builder().id(1L).build();
        when(watchlistRepository.findByUserIdAndMovieId(1L, 1L)).thenReturn(Optional.of(item));

        watchlistService.removeFromWatchlist(1L, 1L);

        verify(watchlistRepository).delete(item);
    }

    @Test
    void removeFromWatchlist_notFound_throws() {
        when(watchlistRepository.findByUserIdAndMovieId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.removeFromWatchlist(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
