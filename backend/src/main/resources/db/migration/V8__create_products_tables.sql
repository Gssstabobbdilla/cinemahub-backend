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