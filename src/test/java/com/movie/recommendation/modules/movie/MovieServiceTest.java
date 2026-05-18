package com.movie.recommendation.modules.movie;

import com.movie.recommendation.exception.BadRequestException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.movie.dto.CreateMovieRequest;
import com.movie.recommendation.modules.movie.dto.MovieDetailResponse;
import com.movie.recommendation.modules.movie.dto.UpdateMovieRequest;
import com.movie.recommendation.modules.movie.entity.Genre;
import com.movie.recommendation.modules.movie.entity.Movie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private MovieService movieService;

    private Genre buildGenre(Integer id, String name) {
        return Genre.builder().id(id).name(name).slug(name.toLowerCase()).build();
    }

    private Movie buildMovie(Long id, String title) {
        return Movie.builder()
                .id(id)
                .title(title)
                .slug(title.toLowerCase().replace(" ", "-"))
                .overview("Overview")
                .avgRating(BigDecimal.ZERO)
                .voteCount(0)
                .isActive(true)
                .genres(new HashSet<>(Set.of(buildGenre(1, "Action"))))
                .build();
    }

    private CreateMovieRequest buildCreateRequest() {
        CreateMovieRequest req = new CreateMovieRequest();
        req.setTitle("Test Movie");
        req.setOverview("Overview");
        req.setReleaseDate(LocalDate.of(2024, 1, 1));
        req.setRuntimeMinutes(120);
        req.setGenreIds(Set.of(1));
        return req;
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllMovies_returnsPaged() {
        Page<Movie> page = new PageImpl<>(List.of(buildMovie(1L, "Movie 1")));
        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = movieService.getAllMovies(0, 10, "title", "asc", null, null, null);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getMovieById_found_returnsDetail() {
        Movie movie = buildMovie(1L, "Test Movie");
        when(movieRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(movie));

        MovieDetailResponse result = movieService.getMovieById(1L);

        assertThat(result.getTitle()).isEqualTo("Test Movie");
    }

    @Test
    void getMovieById_notFound_throws() {
        when(movieRepository.findByIdAndIsActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createMovie_success() {
        CreateMovieRequest request = buildCreateRequest();
        Genre genre = buildGenre(1, "Action");

        when(movieRepository.existsBySlug(anyString())).thenReturn(false);
        when(genreRepository.findAllByIdIn(Set.of(1))).thenReturn(List.of(genre));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> {
            Movie m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        MovieDetailResponse result = movieService.createMovie(request);

        assertThat(result.getTitle()).isEqualTo("Test Movie");
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void createMovie_slugCollision_generatesUnique() {
        CreateMovieRequest request = buildCreateRequest();
        Genre genre = buildGenre(1, "Action");

        when(movieRepository.existsBySlug("test-movie")).thenReturn(true);
        when(genreRepository.findAllByIdIn(Set.of(1))).thenReturn(List.of(genre));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> {
            Movie m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        MovieDetailResponse result = movieService.createMovie(request);

        assertThat(result.getSlug()).isNotEqualTo("test-movie");
        assertThat(result.getSlug()).startsWith("test-movie-");
    }

    @Test
    void createMovie_invalidGenreIds_throws() {
        CreateMovieRequest request = buildCreateRequest();
        request.setGenreIds(Set.of(1, 999));

        when(movieRepository.existsBySlug(anyString())).thenReturn(false);
        when(genreRepository.findAllByIdIn(Set.of(1, 999))).thenReturn(List.of(buildGenre(1, "Action")));

        assertThatThrownBy(() -> movieService.createMovie(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("genre IDs");
    }

    @Test
    void updateMovie_success() {
        Movie existing = buildMovie(1L, "Old Title");
        UpdateMovieRequest request = new UpdateMovieRequest();
        request.setTitle("New Title");
        request.setOverview("New Overview");
        request.setReleaseDate(LocalDate.of(2024, 6, 1));
        request.setRuntimeMinutes(130);
        request.setGenreIds(Set.of(1));

        when(movieRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(movieRepository.existsBySlug("new-title")).thenReturn(false);
        when(genreRepository.findAllByIdIn(Set.of(1))).thenReturn(List.of(buildGenre(1, "Action")));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        MovieDetailResponse result = movieService.updateMovie(1L, request);

        assertThat(result.getTitle()).isEqualTo("New Title");
    }

    @Test
    void softDeleteMovie_success() {
        Movie movie = buildMovie(1L, "Test");
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        movieService.softDeleteMovie(1L);

        assertThat(movie.getIsActive()).isFalse();
        verify(movieRepository).save(movie);
    }

    @Test
    void restoreMovie_success() {
        Movie movie = buildMovie(1L, "Test");
        movie.setIsActive(false);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        MovieDetailResponse result = movieService.restoreMovie(1L);

        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void searchMovies_returnsPaged() {
        Page<Movie> page = new PageImpl<>(List.of(buildMovie(1L, "Inception")));
        when(movieRepository.searchByTitle(eq("Inception"), any(Pageable.class))).thenReturn(page);

        var result = movieService.searchMovies("Inception", null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllMovies_withGenreFilter_returnsFiltered() {
        Page<Movie> page = new PageImpl<>(List.of(buildMovie(1L, "Action Movie")));
        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = movieService.getAllMovies(0, 10, "title", "asc", 1, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(movieRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllMovies_withYearFilter_returnsFiltered() {
        Page<Movie> page = new PageImpl<>(List.of(buildMovie(1L, "Movie 2024")));
        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = movieService.getAllMovies(0, 10, "title", "asc", null, 2024, null);

        assertThat(result.getContent()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllMovies_withMinRatingFilter_returnsFiltered() {
        Page<Movie> page = new PageImpl<>(List.of(buildMovie(1L, "Top Rated")));
        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = movieService.getAllMovies(0, 10, "title", "asc", null, null, new BigDecimal("4.0"));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchMovies_withGenreFilter_returnsFiltered() {
        Page<Movie> page = new PageImpl<>(List.of(buildMovie(1L, "Action Inception")));
        when(movieRepository.searchByTitleAndGenre(eq("Inception"), eq(1), any(Pageable.class)))
                .thenReturn(page);

        var result = movieService.searchMovies("Inception", 1, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(movieRepository).searchByTitleAndGenre(eq("Inception"), eq(1), any(Pageable.class));
    }
}
