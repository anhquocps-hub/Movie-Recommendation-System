CREATE TABLE genres (
    id          SERIAL          PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL UNIQUE,
    slug        VARCHAR(50)     NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE movies (
    id              BIGSERIAL       PRIMARY KEY,
    title           VARCHAR(500)    NOT NULL,
    slug            VARCHAR(500)    NOT NULL UNIQUE,
    overview        TEXT,
    poster_url      VARCHAR(500),
    backdrop_url    VARCHAR(500),
    release_date    DATE,
    runtime_minutes INTEGER,
    avg_rating      NUMERIC(3,2)   NOT NULL DEFAULT 0.00,
    vote_count      INTEGER        NOT NULL DEFAULT 0,
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE movie_genres (
    movie_id    BIGINT  NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    genre_id    INTEGER NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE INDEX idx_movies_active      ON movies (is_active) WHERE is_active = TRUE;
CREATE INDEX idx_movies_release     ON movies (release_date DESC);
CREATE INDEX idx_movies_rating      ON movies (avg_rating DESC);
CREATE INDEX idx_movies_title_trgm  ON movies USING gin (title gin_trgm_ops);
