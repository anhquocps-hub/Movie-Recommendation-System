package com.movie.recommendation.modules.movie;

import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.common.dto.PagedResponse;
import com.movie.recommendation.common.util.SlugUtil;
import com.movie.recommendation.exception.BadRequestException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.movie.dto.*;
import com.movie.recommendation.modules.movie.entity.Genre;
import com.movie.recommendation.modules.movie.entity.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    public PagedResponse<MovieResponse> getAllMovies(int page, int size, String sortBy, String sortDir) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MovieResponse> moviePage = movieRepository.findAllByIsActiveTrue(pageable)
                .map(this::toMovieResponse);
        return PagedResponse.from(moviePage);
    }

    @Cacheable(value = AppConstants.MOVIE_DETAIL_CACHE, key = "#id")
    public MovieDetailResponse getMovieById(Long id) {
        Movie movie = movieRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        return toDetailResponse(movie);
    }

    @Cacheable(value = AppConstants.MOVIE_SEARCH_CACHE, key = "#query + ':' + #page + ':' + #size")
    public PagedResponse<MovieResponse> searchMovies(String query, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieResponse> results = movieRepository.searchByTitle(query, pageable)
                .map(this::toMovieResponse);
        return PagedResponse.from(results);
    }

    @Cacheable(value = AppConstants.TRENDING_CACHE, key = "#page + ':' + #size")
    public PagedResponse<MovieResponse> getTrendingMovies(int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieResponse> trending = movieRepository.findTrending(pageable)
                .map(this::toMovieResponse);
        return PagedResponse.from(trending);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true),
            @CacheEvict(value = AppConstants.MOVIE_SEARCH_CACHE, allEntries = true)
    })
    public MovieDetailResponse createMovie(CreateMovieRequest request) {
        String slug = SlugUtil.slugify(request.getTitle());
        if (movieRepository.existsBySlug(slug)) {
            slug = SlugUtil.uniqueSlug(request.getTitle());
        }

        Set<Genre> genres = resolveGenres(request.getGenreIds());

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .slug(slug)
                .overview(request.getOverview())
                .posterUrl(request.getPosterUrl())
                .backdropUrl(request.getBackdropUrl())
                .releaseDate(request.getReleaseDate())
                .runtimeMinutes(request.getRuntimeMinutes())
                .genres(genres)
                .build();

        return toDetailResponse(movieRepository.save(movie));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.MOVIE_DETAIL_CACHE, key = "#id"),
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true),
            @CacheEvict(value = AppConstants.MOVIE_SEARCH_CACHE, allEntries = true)
    })
    public MovieDetailResponse updateMovie(Long id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        if (!movie.getTitle().equals(request.getTitle())) {
            String slug = SlugUtil.slugify(request.getTitle());
            if (movieRepository.existsBySlug(slug)) {
                slug = SlugUtil.uniqueSlug(request.getTitle());
            }
            movie.setSlug(slug);
        }

        movie.setTitle(request.getTitle());
        movie.setOverview(request.getOverview());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setBackdropUrl(request.getBackdropUrl());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setRuntimeMinutes(request.getRuntimeMinutes());
        movie.setGenres(resolveGenres(request.getGenreIds()));

        return toDetailResponse(movieRepository.save(movie));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.MOVIE_DETAIL_CACHE, key = "#id"),
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true),
            @CacheEvict(value = AppConstants.MOVIE_SEARCH_CACHE, allEntries = true)
    })
    public void softDeleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        movie.setIsActive(false);
        movieRepository.save(movie);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppConstants.MOVIE_DETAIL_CACHE, key = "#id"),
            @CacheEvict(value = AppConstants.TRENDING_CACHE, allEntries = true),
            @CacheEvict(value = AppConstants.MOVIE_SEARCH_CACHE, allEntries = true)
    })
    public MovieDetailResponse restoreMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        movie.setIsActive(true);
        return toDetailResponse(movieRepository.save(movie));
    }

    private Set<Genre> resolveGenres(Set<Integer> genreIds) {
        List<Genre> genres = genreRepository.findAllByIdIn(genreIds);
        if (genres.size() != genreIds.size()) {
            throw new BadRequestException("One or more genre IDs are invalid");
        }
        return new HashSet<>(genres);
    }

    private MovieResponse toMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .posterUrl(movie.getPosterUrl())
                .releaseDate(movie.getReleaseDate())
                .avgRating(movie.getAvgRating())
                .voteCount(movie.getVoteCount())
                .genres(movie.getGenres().stream().map(Genre::getName).toList())
                .build();
    }

    private MovieDetailResponse toDetailResponse(Movie movie) {
        return MovieDetailResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .overview(movie.getOverview())
                .posterUrl(movie.getPosterUrl())
                .backdropUrl(movie.getBackdropUrl())
                .releaseDate(movie.getReleaseDate())
                .runtimeMinutes(movie.getRuntimeMinutes())
                .avgRating(movie.getAvgRating())
                .voteCount(movie.getVoteCount())
                .isActive(movie.getIsActive())
                .genres(movie.getGenres().stream()
                        .map(g -> GenreResponse.builder()
                                .id(g.getId())
                                .name(g.getName())
                                .slug(g.getSlug())
                                .build())
                        .toList())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
