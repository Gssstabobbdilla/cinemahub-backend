-- =========================================================
-- V5: RESERVATION TABLES
-- =========================================================

-- ---------------------------------------------------------
-- reservations
-- ---------------------------------------------------------
CREATE TABLE reservations (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_reservations_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_reservations_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_reservations_user_id ON reservations (user_id);
CREATE INDEX idx_reservations_status_expires_at ON reservations (status, expires_at);

-- ---------------------------------------------------------
-- reservation_seats
-- ---------------------------------------------------------
CREATE TABLE reservation_seats (
    id             BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT        NOT NULL,
    showtime_id    BIGINT        NOT NULL,
    seat_id        BIGINT        NOT NULL,
    price          DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_reservation_seats_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_seats_showtime
        FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_seats_seat
        FOREIGN KEY (seat_id) REFERENCES seats (id) ON DELETE CASCADE,
    CONSTRAINT ck_reservation_seats_price_positive CHECK (price >= 0),
    -- CLAVE: un mismo asiento no puede reservarse dos veces para la misma función.
    CONSTRAINT uq_reservation_seats_showtime_seat UNIQUE (showtime_id, seat_id)
);

CREATE INDEX idx_reservation_seats_reservation_id ON reservation_seats (reservation_id);
CREATE INDEX idx_reservation_seats_showtime_id ON reservation_seats (showtime_id);