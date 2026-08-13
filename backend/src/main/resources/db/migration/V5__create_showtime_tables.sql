-- =========================================================
-- V5: SHOWTIME TABLES
-- =========================================================

CREATE TABLE showtimes (
    id         BIGSERIAL PRIMARY KEY,
    movie_id   BIGINT      NOT NULL,
    room_id    BIGINT      NOT NULL,
    show_date  DATE        NOT NULL,
    start_time TIME        NOT NULL,
    end_time   TIME        NOT NULL,
    language   VARCHAR(30) NOT NULL DEFAULT 'ES',
    format     VARCHAR(20) NOT NULL DEFAULT '2D',
    base_price DECIMAL(10,2) NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    CONSTRAINT fk_showtimes_movie
        FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE,
    CONSTRAINT fk_showtimes_room
        FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE,
    CONSTRAINT ck_showtimes_base_price_positive CHECK (base_price >= 0),
    CONSTRAINT ck_showtimes_time_range CHECK (end_time > start_time),
    CONSTRAINT ck_showtimes_status CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'FINISHED', 'CANCELLED')),
    -- Evita registrar dos funciones exactamente iguales (misma sala, fecha y hora de inicio).
    -- No evita solapamientos parciales; para eso se recomienda una EXCLUDE constraint con btree_gist
    -- más adelante si el negocio lo requiere.
    CONSTRAINT uq_showtimes_room_date_start UNIQUE (room_id, show_date, start_time)
);

CREATE INDEX idx_showtimes_movie_id ON showtimes (movie_id);
CREATE INDEX idx_showtimes_room_date ON showtimes (room_id, show_date);
CREATE INDEX idx_showtimes_status ON showtimes (status);