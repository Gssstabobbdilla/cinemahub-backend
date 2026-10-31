-- CINEMAHUB database schema
-- Run once against a new, empty cinema_db database using pgAdmin or psql.
-- The application does not execute this schema automatically.
-- Source: V1__create_security_tables.sql
-- =========================================================
-- V1: SECURITY TABLES (roles, permissions, users)
-- =========================================================
 
-- Función genérica para mantener updated_at automáticamente
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
 
-- ---------------------------------------------------------
-- roles
-- ---------------------------------------------------------
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_roles_name UNIQUE (name)
);
 
-- ---------------------------------------------------------
-- permissions
-- ---------------------------------------------------------
CREATE TABLE permissions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_permissions_name UNIQUE (name)
);
 
-- ---------------------------------------------------------
-- role_permissions
-- ---------------------------------------------------------
CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);
 
-- ---------------------------------------------------------
-- users
-- ---------------------------------------------------------
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(20),
    birth_date DATE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED', 'PENDING_VERIFICATION'))
);
 
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
 
CREATE INDEX idx_users_status ON users (status);
 
-- ---------------------------------------------------------
-- user_roles
-- ---------------------------------------------------------
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);
 
-- Source: V3__create_movie_tables.sql
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
-- Source: V4__create_cinema_tables.sql
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
-- Source: V5__create_showtime_tables.sql
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
-- Source: V6__create_reservation_tables.sql
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
-- Source: V7__create_order_tables.sql
-- =========================================================
-- V6: ORDER TABLES (orders, payments)
-- =========================================================

-- ---------------------------------------------------------
-- orders
-- ---------------------------------------------------------
CREATE TABLE orders (
    id             BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT        NOT NULL,
    total          DECIMAL(10,2) NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    purchased_at   TIMESTAMPTZ,
    CONSTRAINT fk_orders_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE RESTRICT,
    CONSTRAINT ck_orders_total_positive CHECK (total >= 0),
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'PAID', 'CANCELLED', 'REFUNDED')),
    -- Una reserva solo puede generar una orden.
    CONSTRAINT uq_orders_reservation_id UNIQUE (reservation_id)
);

CREATE INDEX idx_orders_status ON orders (status);

-- ---------------------------------------------------------
-- payments
-- ---------------------------------------------------------
CREATE TABLE payments (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT        NOT NULL,
    payment_method    VARCHAR(30)   NOT NULL,
    transaction_code  VARCHAR(100),
    amount            DECIMAL(10,2) NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid_at           TIMESTAMPTZ,
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT ck_payments_amount_positive CHECK (amount >= 0),
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'REFUNDED')),
    -- Evita procesar el mismo pago dos veces (idempotencia con la pasarela de pago).
    CONSTRAINT uq_payments_transaction_code UNIQUE (transaction_code)
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
-- Source: V8__create_products_tables.sql
-- =========================================================
-- V7: PRODUCTS, INVENTORY & PROMOTIONS TABLES
-- =========================================================

-- ---------------------------------------------------------
-- product_categories
-- ---------------------------------------------------------
CREATE TABLE product_categories (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uq_product_categories_name UNIQUE (name)
);

-- ---------------------------------------------------------
-- products
-- ---------------------------------------------------------
CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT        NOT NULL,
    name        VARCHAR(150)  NOT NULL,
    description VARCHAR(255),
    image_url   VARCHAR(500),
    price       DECIMAL(10,2) NOT NULL,
    stock       INTEGER       NOT NULL DEFAULT 0,
    status      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES product_categories (id) ON DELETE RESTRICT,
    CONSTRAINT ck_products_price_positive CHECK (price >= 0),
    CONSTRAINT ck_products_stock_non_negative CHECK (stock >= 0),
    CONSTRAINT ck_products_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK'))
);

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_status ON products (status);

-- ---------------------------------------------------------
-- order_products
-- ---------------------------------------------------------
CREATE TABLE order_products (
    order_id   BIGINT        NOT NULL,
    product_id BIGINT        NOT NULL,
    quantity   INTEGER       NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_order_products PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_products_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_products_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT ck_order_products_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_order_products_unit_price_positive CHECK (unit_price >= 0)
);

-- ---------------------------------------------------------
-- inventory_movements
-- ---------------------------------------------------------
CREATE TABLE inventory_movements (
    id            BIGSERIAL PRIMARY KEY,
    product_id    BIGINT      NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity      INTEGER     NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_inventory_movements_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT ck_inventory_movements_type CHECK (movement_type IN ('IN', 'OUT', 'ADJUSTMENT')),
    CONSTRAINT ck_inventory_movements_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_inventory_movements_product_id ON inventory_movements (product_id);

-- ---------------------------------------------------------
-- promotions
-- ---------------------------------------------------------
CREATE TABLE promotions (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(150) NOT NULL,
    description         TEXT,
    discount_percentage DECIMAL(5,2) NOT NULL,
    start_date          DATE         NOT NULL,
    end_date            DATE         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT ck_promotions_discount_range CHECK (discount_percentage BETWEEN 0 AND 100),
    CONSTRAINT ck_promotions_date_range CHECK (end_date >= start_date),
    CONSTRAINT ck_promotions_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED'))
);

CREATE INDEX idx_promotions_status ON promotions (status);

-- ---------------------------------------------------------
-- coupons
-- ---------------------------------------------------------
CREATE TABLE coupons (
    id                  BIGSERIAL PRIMARY KEY,
    promotion_id        BIGINT       NOT NULL,
    code                VARCHAR(50)  NOT NULL,
    discount_percentage DECIMAL(5,2) NOT NULL,
    expires_at          TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_coupons_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotions (id) ON DELETE CASCADE,
    CONSTRAINT uq_coupons_code UNIQUE (code),
    CONSTRAINT ck_coupons_discount_range CHECK (discount_percentage BETWEEN 0 AND 100)
);

CREATE INDEX idx_coupons_promotion_id ON coupons (promotion_id);
-- Source: V9__create_membership_tables.sql
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
