package com.movie.recommendation.modules.movie;

import com.movie.recommendation.common.constants.AppConstants;
import com.movie.recommendation.common.util.SlugUtil;
import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.movie.dto.CreateGenreRequest;
import com.movie.recommendation.modules.movie.dto.GenreResponse;
import com.movie.recommendation.modules.movie.dto.UpdateGenreRequest;
import com.movie.recommendation.modules.movie.entity.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    @Cacheable(AppConstants.GENRE_CACHE)
    public List<GenreResponse> getAllGenres() {
        return new ArrayList<>(genreRepository.findAll().stream()
                .map(this::toResponse)
                .toList());
    }

    @Transactional
    @CacheEvict(value = AppConstants.GENRE_CACHE, allEntries = true)
    public GenreResponse createGenre(CreateGenreRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Genre", "name", request.getName());
        }

        String slug = SlugUtil.slugify(request.getName());
        if (genreRepository.existsBySlug(slug)) {
            slug = SlugUtil.uniqueSlug(request.getName());
        }

        Genre genre = Genre.builder()
                .name(request.getName())
                .slug(slug)
                .build();

        return toResponse(genreRepository.save(genre));
    }

    @Transactional
    @CacheEvict(value = AppConstants.GENRE_CACHE, allEntries = true)
    public GenreResponse updateGenre(Integer id, UpdateGenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));

        if (!genre.getName().equals(request.getName()) && genreRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Genre", "name", request.getName());
        }

        genre.setName(request.getName());
        String slug = SlugUtil.slugify(request.getName());
        if (!slug.equals(genre.getSlug()) && genreRepository.existsBySlug(slug)) {
            slug = SlugUtil.uniqueSlug(request.getName());
        }
        genre.setSlug(slug);

        return toResponse(genreRepository.save(genre));
    }

    @Transactional
    @CacheEvict(value = AppConstants.GENRE_CACHE, allEntries = true)
    public void deleteGenre(Integer id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));
        genreRepository.delete(genre);
    }

    private GenreResponse toResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .slug(genre.getSlug())
                .build();
    }
}
