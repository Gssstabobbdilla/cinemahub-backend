-- =========================================================
-- V8: MEMBERSHIP & NOTIFICATION TABLES
-- =========================================================

-- ---------------------------------------------------------
-- memberships
-- ---------------------------------------------------------
CREATE TABLE memberships (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT      NOT NULL,
    level   VARCHAR(20) NOT NULL DEFAULT 'BASIC',
    points  INTEGER     NOT NULL DEFAULT 0,
    CONSTRAINT fk_memberships_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_memberships_level CHECK (level IN ('BASIC', 'SILVER', 'GOLD', 'PLATINUM')),
    CONSTRAINT ck_memberships_points_non_negative CHECK (points >= 0),
    -- Un usuario tiene, como máximo, una membresía.
    CONSTRAINT uq_memberships_user_id UNIQUE (user_id)
);

-- ---------------------------------------------------------
-- point_history
-- ---------------------------------------------------------
CREATE TABLE point_history (
    id             BIGSERIAL PRIMARY KEY,
    membership_id  BIGINT      NOT NULL,
    points         INTEGER     NOT NULL,
    reason         VARCHAR(150),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_point_history_membership
        FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE CASCADE
);

CREATE INDEX idx_point_history_membership_id ON point_history (membership_id);

-- ---------------------------------------------------------
-- notifications
-- ---------------------------------------------------------
CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    title      VARCHAR(150) NOT NULL,
    message    TEXT,
    is_read    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id_is_read ON notifications (user_id, is_read);