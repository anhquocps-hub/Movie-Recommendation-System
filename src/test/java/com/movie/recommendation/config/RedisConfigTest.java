package com.movie.recommendation.config;

import com.movie.recommendation.common.constants.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    @Test
    void cacheManager_hasPerCacheTtlConfigurations() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer("@class")))
                .prefixCacheNameWith("movie-rec::")
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> perCacheConfig = Map.of(
                AppConstants.GENRE_CACHE, defaultConfig.entryTtl(Duration.ofHours(24)),
                AppConstants.MOVIE_DETAIL_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)),
                AppConstants.MOVIE_SEARCH_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)),
                AppConstants.TRENDING_CACHE, defaultConfig.entryTtl(Duration.ofHours(1))
        );

        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfig)
                .enableCreateOnMissingCache()
                .build();

        cacheManager.initializeCaches();

        Map<String, RedisCacheConfiguration> configs = cacheManager.getCacheConfigurations();

        assertThat(configs).containsKeys(
                AppConstants.GENRE_CACHE,
                AppConstants.MOVIE_DETAIL_CACHE,
                AppConstants.MOVIE_SEARCH_CACHE,
                AppConstants.TRENDING_CACHE);

        assertThat(configs.get(AppConstants.GENRE_CACHE).getTtl())
                .isEqualTo(Duration.ofHours(24));

        assertThat(configs.get(AppConstants.MOVIE_SEARCH_CACHE).getTtl())
                .isEqualTo(Duration.ofMinutes(30));

        assertThat(configs.get(AppConstants.TRENDING_CACHE).getTtl())
                .isEqualTo(Duration.ofHours(1));

        assertThat(configs.get(AppConstants.MOVIE_DETAIL_CACHE).getTtl())
                .isEqualTo(Duration.ofHours(1));
    }
}
