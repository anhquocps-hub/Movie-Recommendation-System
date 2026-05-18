CREATE TABLE reviews (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movie_id    BIGINT          NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    rating      SMALLINT        NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content     TEXT,
    is_spoiler  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, movie_id)
);

CREATE TABLE review_likes (
    id          BIGSERIAL   PRIMARY KEY,
    review_id   BIGINT      NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (review_id, user_id)
);

CREATE TABLE review_replies (
    id          BIGSERIAL   PRIMARY KEY,
    review_id   BIGINT      NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_movie  ON reviews (movie_id);
CREATE INDEX idx_reviews_user   ON reviews (user_id);
CREATE INDEX idx_replies_review ON review_replies (review_id);
