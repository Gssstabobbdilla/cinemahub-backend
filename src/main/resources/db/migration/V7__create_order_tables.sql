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