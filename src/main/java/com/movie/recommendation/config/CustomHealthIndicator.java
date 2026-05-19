package com.movie.recommendation.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            boolean dbHealthy = checkDatabase();
            boolean redisHealthy = checkRedis();

            if (dbHealthy && redisHealthy) {
                return Health.up()
                        .withDetail("database", "UP")
                        .withDetail("redis", "UP")
                        .build();
            } else if (!dbHealthy && !redisHealthy) {
                return Health.down()
                        .withDetail("database", "DOWN")
                        .withDetail("redis", "DOWN")
                        .build();
            } else if (!dbHealthy) {
                return Health.down()
                        .withDetail("database", "DOWN")
                        .withDetail("redis", "UP")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "UP")
                        .withDetail("redis", "DOWN")
                        .build();
            }
        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return false;
        }
    }
}
