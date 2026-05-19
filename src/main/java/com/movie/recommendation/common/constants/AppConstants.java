package com.movie.recommendation.common.constants;

public final class AppConstants {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";

    public static final long TRENDING_CACHE_TTL = 3600;
    public static final long SEARCH_CACHE_TTL = 1800;

    public static final int BCRYPT_STRENGTH = 12;

    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    public static final String PASSWORD_RESET_PREFIX = "password-reset::";
    public static final String BLACKLISTED_REFRESH_PREFIX = "blacklisted-refresh::";
    public static final long PASSWORD_RESET_TTL_MINUTES = 15;

    public static final String TRENDING_CACHE = "trending_movies";
    public static final String GENRE_CACHE = "genres_all";
    public static final String MOVIE_DETAIL_CACHE = "movie_detail";
    public static final String MOVIE_SEARCH_CACHE = "movie_search";
    public static final String RECOMMENDATION_CACHE = "user_recommendations";

    private AppConstants() {
    }
}
