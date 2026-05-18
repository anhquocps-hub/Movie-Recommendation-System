package com.movie.recommendation.modules.movie;

import com.movie.recommendation.exception.DuplicateResourceException;
import com.movie.recommendation.exception.ResourceNotFoundException;
import com.movie.recommendation.modules.movie.dto.CreateGenreRequest;
import com.movie.recommendation.modules.movie.dto.GenreResponse;
import com.movie.recommendation.modules.movie.dto.UpdateGenreRequest;
import com.movie.recommendation.modules.movie.entity.Genre;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    private Genre buildGenre(Integer id, String name) {
        return Genre.builder().id(id).name(name).slug(name.toLowerCase().replace(" ", "-")).build();
    }

    @Test
    void getAllGenres_returnsList() {
        when(genreRepository.findAll()).thenReturn(List.of(
                buildGenre(1, "Action"),
                buildGenre(2, "Comedy")
        ));

        List<GenreResponse> result = genreService.getAllGenres();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Action");
    }

    @Test
    void createGenre_success() {
        CreateGenreRequest request = new CreateGenreRequest();
        request.setName("Drama");

        when(genreRepository.existsByName("Drama")).thenReturn(false);
        when(genreRepository.existsBySlug("drama")).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> {
            Genre g = inv.getArgument(0);
            g.setId(1);
            return g;
        });

        GenreResponse result = genreService.createGenre(request);

        assertThat(result.getName()).isEqualTo("Drama");
        assertThat(result.getSlug()).isEqualTo("drama");
        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    void createGenre_duplicateName_throws() {
        CreateGenreRequest request = new CreateGenreRequest();
        request.setName("Action");

        when(genreRepository.existsByName("Action")).thenReturn(true);

        assertThatThrownBy(() -> genreService.createGenre(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateGenre_success() {
        Genre existing = buildGenre(1, "Action");
        UpdateGenreRequest request = new UpdateGenreRequest();
        request.setName("Adventure");

        when(genreRepository.findById(1)).thenReturn(Optional.of(existing));
        when(genreRepository.existsByName("Adventure")).thenReturn(false);
        when(genreRepository.existsBySlug("adventure")).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        GenreResponse result = genreService.updateGenre(1, request);

        assertThat(result.getName()).isEqualTo("Adventure");
    }

    @Test
    void updateGenre_notFound_throws() {
        UpdateGenreRequest request = new UpdateGenreRequest();
        request.setName("Horror");

        when(genreRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.updateGenre(99, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteGenre_success() {
        Genre existing = buildGenre(1, "Action");
        when(genreRepository.findById(1)).thenReturn(Optional.of(existing));

        genreService.deleteGenre(1);

        verify(genreRepository).delete(existing);
    }
}
