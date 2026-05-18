CREATE TABLE watchlist (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movie_id    BIGINT      NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, movie_id)
);

CREATE INDEX idx_watchlist_user ON watchlist (user_id);
