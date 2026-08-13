-- =========================================================
-- V3: MOVIE TABLES
-- =========================================================

-- ---------------------------------------------------------
-- classifications  (ej: G, PG, PG-13, R)
-- ---------------------------------------------------------
CREATE TABLE classifications (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(10)  NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_classifications_code UNIQUE (code)
);

-- ---------------------------------------------------------
-- genres
-- ---------------------------------------------------------
CREATE TABLE genres (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uq_genres_name UNIQUE (name)
);

-- ---------------------------------------------------------
-- movies
-- ---------------------------------------------------------
CREATE TABLE movies (
    id                 BIGSERIAL PRIMARY KEY,
    title              VARCHAR(200) NOT NULL,
    synopsis           TEXT,
    duration           INTEGER      NOT NULL,
    release_date       DATE,
    poster_url         VARCHAR(500),
    trailer_url        VARCHAR(500),
    classification_id  BIGINT       NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_movies_classification
        FOREIGN KEY (classification_id) REFERENCES classifications (id) ON DELETE RESTRICT,
    CONSTRAINT ck_movies_duration_positive CHECK (duration > 0),
    CONSTRAINT ck_movies_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'COMING_SOON', 'ARCHIVED'))
);

CREATE TRIGGER trg_movies_updated_at
    BEFORE UPDATE ON movies
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_movies_status ON movies (status);
CREATE INDEX idx_movies_release_date ON movies (release_date);
CREATE INDEX idx_movies_classification_id ON movies (classification_id);

-- ---------------------------------------------------------
-- movie_genres
-- ---------------------------------------------------------
CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    CONSTRAINT pk_movie_genres PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_movie_genres_movie
        FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE,
    CONSTRAINT fk_movie_genres_genre
        FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE CASCADE
);