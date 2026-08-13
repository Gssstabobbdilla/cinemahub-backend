-- =========================================================
-- V4: CINEMA TABLES (cinemas, rooms, seats)
-- =========================================================

-- ---------------------------------------------------------
-- cinemas
-- ---------------------------------------------------------
CREATE TABLE cinemas (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    department VARCHAR(100),
    province   VARCHAR(100),
    district   VARCHAR(100),
    address    VARCHAR(255),
    phone      VARCHAR(20),
    latitude   DECIMAL(9,6),
    longitude  DECIMAL(9,6),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_cinemas_updated_at
    BEFORE UPDATE ON cinemas
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_cinemas_department_province_district
    ON cinemas (department, province, district);

-- ---------------------------------------------------------
-- rooms
-- ---------------------------------------------------------
CREATE TABLE rooms (
    id        BIGSERIAL PRIMARY KEY,
    cinema_id BIGINT      NOT NULL,
    name      VARCHAR(50) NOT NULL,
    type      VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    capacity  INTEGER     NOT NULL,
    CONSTRAINT fk_rooms_cinema
        FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE CASCADE,
    CONSTRAINT ck_rooms_capacity_positive CHECK (capacity > 0),
    CONSTRAINT uq_rooms_cinema_name UNIQUE (cinema_id, name)
);

CREATE INDEX idx_rooms_cinema_id ON rooms (cinema_id);

-- ---------------------------------------------------------
-- seats
-- ---------------------------------------------------------
CREATE TABLE seats (
    id          BIGSERIAL PRIMARY KEY,
    room_id     BIGINT      NOT NULL,
    row_label   VARCHAR(5)  NOT NULL,
    seat_number INTEGER     NOT NULL,
    seat_type   VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    CONSTRAINT fk_seats_room
        FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE,
    CONSTRAINT uq_seats_room_row_number UNIQUE (room_id, row_label, seat_number)
);

CREATE INDEX idx_seats_room_id ON seats (room_id);