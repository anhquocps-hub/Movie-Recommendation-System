package com.movie.recommendation.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomHealthIndicatorTest {

    private DataSource dataSource;
    private RedisConnectionFactory redisConnectionFactory;
    private CustomHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        redisConnectionFactory = mock(RedisConnectionFactory.class);
        healthIndicator = new CustomHealthIndicator(dataSource, redisConnectionFactory);
    }

    @Test
    void health_bothHealthy_returnsUp() throws Exception {
        Connection dbConnection = mock(Connection.class);
        when(dbConnection.isValid(2)).thenReturn(true);
        when(dataSource.getConnection()).thenReturn(dbConnection);

        RedisConnection redisConnection = mock(RedisConnection.class);
        when(redisConnection.ping()).thenReturn("PONG");
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("database", "UP");
        assertThat(health.getDetails()).containsEntry("redis", "UP");
    }

    @Test
    void health_databaseDown_returnsDown() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        RedisConnection redisConnection = mock(RedisConnection.class);
        when(redisConnection.ping()).thenReturn("PONG");
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "DOWN");
        assertThat(health.getDetails()).containsEntry("redis", "UP");
    }

    @Test
    void health_redisDown_returnsDown() throws Exception {
        Connection dbConnection = mock(Connection.class);
        when(dbConnection.isValid(2)).thenReturn(true);
        when(dataSource.getConnection()).thenReturn(dbConnection);

        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("Redis unavailable"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "UP");
        assertThat(health.getDetails()).containsEntry("redis", "DOWN");
    }

    @Test
    void health_bothDown_returnsDown() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));
        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("Redis unavailable"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "DOWN");
        assertThat(health.getDetails()).containsEntry("redis", "DOWN");
    }

    @Test
    void health_dbConnectionInvalid_returnsDown() throws Exception {
        Connection dbConnection = mock(Connection.class);
        when(dbConnection.isValid(2)).thenReturn(false);
        when(dataSource.getConnection()).thenReturn(dbConnection);

        RedisConnection redisConnection = mock(RedisConnection.class);
        when(redisConnection.ping()).thenReturn("PONG");
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "DOWN");
    }
}
