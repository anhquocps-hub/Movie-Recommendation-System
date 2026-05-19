package com.movie.recommendation.config;

import com.movie.recommendation.modules.movie.GenreRepository;
import com.movie.recommendation.modules.movie.MovieRepository;
import com.movie.recommendation.modules.movie.entity.Genre;
import com.movie.recommendation.modules.movie.entity.Movie;
import com.movie.recommendation.modules.notification.NotificationRepository;
import com.movie.recommendation.modules.notification.entity.Notification;
import com.movie.recommendation.modules.recommendation.RecommendationRepository;
import com.movie.recommendation.modules.recommendation.entity.Recommendation;
import com.movie.recommendation.modules.review.ReviewLikeRepository;
import com.movie.recommendation.modules.review.ReviewReplyRepository;
import com.movie.recommendation.modules.review.ReviewRepository;
import com.movie.recommendation.modules.review.entity.Review;
import com.movie.recommendation.modules.review.entity.ReviewLike;
import com.movie.recommendation.modules.review.entity.ReviewReply;
import com.movie.recommendation.modules.user.UserRepository;
import com.movie.recommendation.modules.user.entity.Role;
import com.movie.recommendation.modules.user.entity.User;
import com.movie.recommendation.modules.watchlist.WatchlistRepository;
import com.movie.recommendation.modules.watchlist.entity.WatchlistItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder {

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final WatchlistRepository watchlistRepository;
    private final RecommendationRepository recommendationRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedData() {
        return args -> seed();
    }

    @Transactional
    public void seed() {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        log.info("Seeding development data...");

        User admin = saveUser("admin@movie.com", "admin", "password", Role.ADMIN, "[\"action\",\"sci-fi\"]");
        User alice = saveUser("alice@test.com", "alice", "password", Role.USER, "[\"drama\",\"romance\"]");
        User bob = saveUser("bob@test.com", "bob", "password", Role.USER, "[\"action\",\"thriller\"]");
        User charlie = saveUser("charlie@test.com", "charlie", "password", Role.USER, "[\"comedy\",\"animation\"]");
        User diana = saveUser("diana@test.com", "diana", "password", Role.USER, "[\"horror\",\"thriller\"]");

        log.info("Created {} users", userRepository.count());

        Genre action = saveGenre("Action", "action");
        Genre drama = saveGenre("Drama", "drama");
        Genre sciFi = saveGenre("Sci-Fi", "sci-fi");
        Genre comedy = saveGenre("Comedy", "comedy");
        Genre horror = saveGenre("Horror", "horror");
        Genre thriller = saveGenre("Thriller", "thriller");
        Genre romance = saveGenre("Romance", "romance");
        Genre animation = saveGenre("Animation", "animation");
        Genre adventure = saveGenre("Adventure", "adventure");
        Genre crime = saveGenre("Crime", "crime");

        log.info("Created {} genres", genreRepository.count());

        Movie inception = saveMovie("Inception", "inception",
                "A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea.",
                LocalDate.of(2010, 7, 16), 148, Set.of(action, sciFi, thriller));

        Movie shawshank = saveMovie("The Shawshank Redemption", "the-shawshank-redemption",
                "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                LocalDate.of(1994, 9, 23), 142, Set.of(drama, crime));

        Movie darkKnight = saveMovie("The Dark Knight", "the-dark-knight",
                "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests.",
                LocalDate.of(2008, 7, 18), 152, Set.of(action, crime, thriller));

        Movie pulpFiction = saveMovie("Pulp Fiction", "pulp-fiction",
                "The lives of two mob hitmen, a boxer, a gangster and his wife intertwine in four tales of violence and redemption.",
                LocalDate.of(1994, 10, 14), 154, Set.of(crime, drama));

        Movie forrestGump = saveMovie("Forrest Gump", "forrest-gump",
                "The presidencies of Kennedy and Johnson, the Vietnam War, and other historical events unfold from the perspective of an Alabama man.",
                LocalDate.of(1994, 7, 6), 142, Set.of(drama, romance));

        Movie matrix = saveMovie("The Matrix", "the-matrix",
                "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.",
                LocalDate.of(1999, 3, 31), 136, Set.of(action, sciFi));

        Movie interstellar = saveMovie("Interstellar", "interstellar",
                "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
                LocalDate.of(2014, 11, 7), 169, Set.of(adventure, drama, sciFi));

        Movie parasite = saveMovie("Parasite", "parasite",
                "Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.",
                LocalDate.of(2019, 5, 30), 132, Set.of(drama, thriller));

        Movie toyStory = saveMovie("Toy Story", "toy-story",
                "A cowboy doll is profoundly threatened and jealous when a new spaceman figure supplants him as top toy in a boy's room.",
                LocalDate.of(1995, 11, 22), 81, Set.of(animation, adventure, comedy));

        Movie shining = saveMovie("The Shining", "the-shining",
                "A family heads to an isolated hotel for the winter where a sinister presence influences the father into violence.",
                LocalDate.of(1980, 5, 23), 146, Set.of(horror, thriller));

        Movie fightClub = saveMovie("Fight Club", "fight-club",
                "An insomniac office worker and a devil-may-care soap maker form an underground fight club that evolves into much more.",
                LocalDate.of(1999, 10, 15), 139, Set.of(drama, thriller));

        Movie godfather = saveMovie("The Godfather", "the-godfather",
                "The aging patriarch of an organized crime dynasty transfers control to his reluctant youngest son.",
                LocalDate.of(1972, 3, 24), 175, Set.of(crime, drama));

        Movie spiritedAway = saveMovie("Spirited Away", "spirited-away",
                "During her family's move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits.",
                LocalDate.of(2001, 7, 20), 125, Set.of(animation, adventure));

        Movie silence = saveMovie("The Silence of the Lambs", "the-silence-of-the-lambs",
                "A young FBI cadet must receive the help of an incarcerated and manipulative cannibal killer to help catch another serial killer.",
                LocalDate.of(1991, 2, 14), 118, Set.of(crime, horror, thriller));

        Movie goodfellas = saveMovie("Goodfellas", "goodfellas",
                "The story of Henry Hill and his life in the mob, covering his relationship with his wife Karen Hill and his mob partners.",
                LocalDate.of(1990, 9, 19), 146, Set.of(crime, drama));

        log.info("Created {} movies", movieRepository.count());

        // Reviews — each user reviews several movies to give the frontend variety
        saveReview(alice, inception, (short) 5, "Mind-blowing! One of the best sci-fi films ever made.");
        saveReview(bob, inception, (short) 4, "Great concept and execution, though a bit confusing at times.");
        saveReview(charlie, inception, (short) 5, "Nolan at his finest. The dream sequences are incredible.");

        saveReview(alice, shawshank, (short) 5, "A masterpiece. The story of hope and friendship is timeless.");
        saveReview(bob, shawshank, (short) 5, "Perfect in every way. Best film I've ever seen.");
        saveReview(diana, shawshank, (short) 5, "Emotional and powerful. A must-watch.");

        saveReview(bob, darkKnight, (short) 5, "Heath Ledger's Joker is legendary. Best superhero movie ever.");
        saveReview(charlie, darkKnight, (short) 4, "Dark and intense. Christian Bale was great as Batman.");
        saveReview(diana, darkKnight, (short) 5, "The action scenes are phenomenal!");

        saveReview(alice, pulpFiction, (short) 4, "Tarantino's storytelling is unique and captivating.");
        saveReview(charlie, pulpFiction, (short) 5, "Non-linear narrative done right. Iconic dialogues.");

        saveReview(alice, forrestGump, (short) 5, "Heartwarming and beautifully told. Tom Hanks is perfect.");
        saveReview(diana, forrestGump, (short) 4, "A touching story that spans decades. Very emotional.");

        saveReview(bob, matrix, (short) 5, "Revolutionary. Changed action movies forever.");
        saveReview(charlie, matrix, (short) 4, "The special effects were groundbreaking for its time.");
        saveReview(diana, matrix, (short) 5, "The philosophical themes are fascinating.");

        saveReview(alice, interstellar, (short) 5, "Visually stunning and emotionally powerful.");
        saveReview(bob, interstellar, (short) 4, "Great sci-fi, though the ending was a bit confusing.");

        saveReview(alice, parasite, (short) 5, "A brilliant social commentary. Deserved all the awards.");
        saveReview(diana, parasite, (short) 5, "Gripping from start to finish. The twist was unexpected.");

        saveReview(charlie, toyStory, (short) 5, "A classic that started it all. Pixar's best work.");
        saveReview(alice, toyStory, (short) 4, "Nostalgic and fun. Great for all ages.");

        saveReview(diana, shining, (short) 5, "Terrifying and atmospheric. Kubrick's masterpiece.");
        saveReview(bob, shining, (short) 4, "Creepy and unsettling. Jack Nicholson was perfect.");

        saveReview(bob, fightClub, (short) 5, "One of the greatest plot twists in cinema history.");
        saveReview(alice, fightClub, (short) 4, "Thought-provoking and intense. Great performances.");
        saveReview(diana, fightClub, (short) 4, "Dark and thrilling. Edward Norton was fantastic.");

        saveReview(alice, godfather, (short) 5, "The definitive crime film. Marlon Brando is iconic.");
        saveReview(bob, godfather, (short) 5, "A flawless masterpiece. The acting is unparalleled.");

        saveReview(charlie, spiritedAway, (short) 5, "Miyazaki's greatest work. Beautiful in every way.");
        saveReview(alice, spiritedAway, (short) 5, "Magical and enchanting. A perfect animated film.");

        saveReview(diana, silence, (short) 5, "Absolutely chilling. Anthony Hopkins is terrifying.");
        saveReview(bob, silence, (short) 4, "A masterclass in suspense. Jodie Foster was brilliant.");

        saveReview(alice, goodfellas, (short) 4, "Scorsese at his best. The pacing is relentless.");
        saveReview(bob, goodfellas, (short) 5, "The best mob movie ever made. Period.");

        log.info("Created {} reviews", reviewRepository.count());

        // Likes and replies
        List<Review> allReviews = reviewRepository.findAll();
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(0)).user(bob).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(0)).user(charlie).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(0)).user(diana).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(1)).user(alice).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(3)).user(bob).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(3)).user(charlie).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(6)).user(alice).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(9)).user(bob).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(13)).user(alice).build());
        reviewLikeRepository.save(ReviewLike.builder().review(allReviews.get(24)).user(charlie).build());

        log.info("Created {} review likes", reviewLikeRepository.count());

        reviewReplyRepository.save(ReviewReply.builder().review(allReviews.get(0)).user(bob).content("Totally agree! The ending blew my mind.").build());
        reviewReplyRepository.save(ReviewReply.builder().review(allReviews.get(0)).user(charlie).content("I've watched it 3 times and still find new details.").build());
        reviewReplyRepository.save(ReviewReply.builder().review(allReviews.get(3)).user(charlie).content("Couldn't agree more. A true classic.").build());
        reviewReplyRepository.save(ReviewReply.builder().review(allReviews.get(6)).user(diana).content("Heath Ledger deserved that Oscar!").build());
        reviewReplyRepository.save(ReviewReply.builder().review(allReviews.get(13)).user(charlie).content("The bullet-time sequences were mind-blowing.").build());

        log.info("Created {} review replies", reviewReplyRepository.count());

        // Watchlist
        watchlistRepository.save(WatchlistItem.builder().user(alice).movie(matrix).build());
        watchlistRepository.save(WatchlistItem.builder().user(alice).movie(darkKnight).build());
        watchlistRepository.save(WatchlistItem.builder().user(alice).movie(silence).build());
        watchlistRepository.save(WatchlistItem.builder().user(bob).movie(parasite).build());
        watchlistRepository.save(WatchlistItem.builder().user(bob).movie(forrestGump).build());
        watchlistRepository.save(WatchlistItem.builder().user(bob).movie(spiritedAway).build());
        watchlistRepository.save(WatchlistItem.builder().user(charlie).movie(shawshank).build());
        watchlistRepository.save(WatchlistItem.builder().user(charlie).movie(interstellar).build());
        watchlistRepository.save(WatchlistItem.builder().user(charlie).movie(godfather).build());
        watchlistRepository.save(WatchlistItem.builder().user(diana).movie(inception).build());
        watchlistRepository.save(WatchlistItem.builder().user(diana).movie(pulpFiction).build());
        watchlistRepository.save(WatchlistItem.builder().user(diana).movie(toyStory).build());

        log.info("Created {} watchlist items", watchlistRepository.count());

        // Recommendations
        recommendationRepository.save(Recommendation.builder().user(alice).movie(matrix).score(BigDecimal.valueOf(0.92)).strategyType(Recommendation.StrategyType.COLLABORATIVE).build());
        recommendationRepository.save(Recommendation.builder().user(alice).movie(darkKnight).score(BigDecimal.valueOf(0.89)).strategyType(Recommendation.StrategyType.COLLABORATIVE).build());
        recommendationRepository.save(Recommendation.builder().user(alice).movie(silence).score(BigDecimal.valueOf(0.85)).strategyType(Recommendation.StrategyType.CONTENT_BASED).build());
        recommendationRepository.save(Recommendation.builder().user(alice).movie(fightClub).score(BigDecimal.valueOf(0.82)).strategyType(Recommendation.StrategyType.CONTENT_BASED).build());

        recommendationRepository.save(Recommendation.builder().user(bob).movie(parasite).score(BigDecimal.valueOf(0.94)).strategyType(Recommendation.StrategyType.COLLABORATIVE).build());
        recommendationRepository.save(Recommendation.builder().user(bob).movie(forrestGump).score(BigDecimal.valueOf(0.88)).strategyType(Recommendation.StrategyType.COLLABORATIVE).build());
        recommendationRepository.save(Recommendation.builder().user(bob).movie(spiritedAway).score(BigDecimal.valueOf(0.83)).strategyType(Recommendation.StrategyType.CONTENT_BASED).build());

        recommendationRepository.save(Recommendation.builder().user(charlie).movie(godfather).score(BigDecimal.valueOf(0.90)).strategyType(Recommendation.StrategyType.COLLABORATIVE).build());
        recommendationRepository.save(Recommendation.builder().user(charlie).movie(shawshank).score(BigDecimal.valueOf(0.87)).strategyType(Recommendation.StrategyType.CONTENT_BASED).build());
        recommendationRepository.save(Recommendation.builder().user(charlie).movie(interstellar).score(BigDecimal.valueOf(0.84)).strategyType(Recommendation.StrategyType.CONTENT_BASED).build());

        recommendationRepository.save(Recommendation.builder().user(diana).movie(inception).score(BigDecimal.valueOf(0.91)).strategyType(Recommendation.StrategyType.COLLABORATIVE).build());
        recommendationRepository.save(Recommendation.builder().user(diana).movie(toyStory).score(BigDecimal.valueOf(0.86)).strategyType(Recommendation.StrategyType.CONTENT_BASED).build());
        recommendationRepository.save(Recommendation.builder().user(diana).movie(pulpFiction).score(BigDecimal.valueOf(0.84)).strategyType(Recommendation.StrategyType.CONTENT_BASED).build());

        log.info("Created {} recommendations", recommendationRepository.count());

        // Notifications
        notificationRepository.save(Notification.builder().recipient(alice).actor(bob).type(Notification.NotificationType.REVIEW_LIKE).referenceId(allReviews.get(0).getId()).message("bob liked your review on Inception").isRead(false).build());
        notificationRepository.save(Notification.builder().recipient(alice).actor(charlie).type(Notification.NotificationType.REVIEW_LIKE).referenceId(allReviews.get(0).getId()).message("charlie liked your review on Inception").isRead(false).build());
        notificationRepository.save(Notification.builder().recipient(alice).actor(diana).type(Notification.NotificationType.REVIEW_LIKE).referenceId(allReviews.get(0).getId()).message("diana liked your review on Inception").isRead(true).build());
        notificationRepository.save(Notification.builder().recipient(alice).actor(bob).type(Notification.NotificationType.REVIEW_REPLY).referenceId(allReviews.get(0).getId()).message("bob replied to your review on Inception").isRead(false).build());
        notificationRepository.save(Notification.builder().recipient(alice).actor(null).type(Notification.NotificationType.NEW_RECOMMENDATION).referenceId(null).message("New movie recommendations are available for you!").isRead(false).build());

        notificationRepository.save(Notification.builder().recipient(bob).actor(alice).type(Notification.NotificationType.REVIEW_LIKE).referenceId(allReviews.get(1).getId()).message("alice liked your review on Inception").isRead(false).build());
        notificationRepository.save(Notification.builder().recipient(bob).actor(null).type(Notification.NotificationType.NEW_RECOMMENDATION).referenceId(null).message("New movie recommendations are available for you!").isRead(true).build());

        notificationRepository.save(Notification.builder().recipient(charlie).actor(alice).type(Notification.NotificationType.REVIEW_LIKE).referenceId(allReviews.get(24).getId()).message("alice liked your review on Fight Club").isRead(false).build());

        log.info("Created {} notifications", notificationRepository.count());

        log.info("Development data seeding completed!");
        log.info("Login credentials (all passwords: \"password\"):");
        log.info("  Admin: admin@movie.com");
        log.info("  Users: alice@test.com, bob@test.com, charlie@test.com, diana@test.com");
    }

    private User saveUser(String email, String username, String password, Role role, String preferences) {
        return userRepository.save(User.builder()
                .email(email).username(username)
                .passwordHash(passwordEncoder.encode(password))
                .role(role).preferences(preferences).isActive(true).build());
    }

    private Genre saveGenre(String name, String slug) {
        return genreRepository.save(Genre.builder().name(name).slug(slug).build());
    }

    private Movie saveMovie(String title, String slug, String overview, LocalDate releaseDate,
                           int runtimeMinutes, Set<Genre> genres) {
        return movieRepository.save(Movie.builder()
                .title(title).slug(slug).overview(overview)
                .releaseDate(releaseDate).runtimeMinutes(runtimeMinutes)
                .avgRating(BigDecimal.ZERO).voteCount(0).isActive(true)
                .genres(genres).build());
    }

    private void saveReview(User user, Movie movie, short rating, String content) {
        reviewRepository.save(Review.builder()
                .user(user).movie(movie).rating(rating)
                .content(content).isSpoiler(false).build());

        Double avg = reviewRepository.calculateAverageRating(movie.getId());
        int count = reviewRepository.countByMovieId(movie.getId());
        movie.setAvgRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        movie.setVoteCount(count);
        movieRepository.save(movie);
    }
}
