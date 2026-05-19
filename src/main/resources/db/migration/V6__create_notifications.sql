CREATE TABLE notifications (
    id              BIGSERIAL       PRIMARY KEY,
    recipient_id    BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id        BIGINT          REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(30)     NOT NULL
                                    CHECK (type IN ('REVIEW_LIKE', 'REVIEW_REPLY', 'NEW_RECOMMENDATION')),
    reference_id    BIGINT,
    message         TEXT            NOT NULL,
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_recipient ON notifications (recipient_id, is_read, created_at DESC);
