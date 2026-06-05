-- Baseline schema for the Inventory Management System.
-- Mirrors the JPA entities in com.firomsa.inventory.model as of the initial Flyway adoption.
-- Hibernate runs in `validate` mode against this schema, so column names/types must match what
-- the entity mappings expect (Spring physical naming strategy -> snake_case).

-- Sequences for Integer @GeneratedValue ids (Hibernate AUTO -> SEQUENCE, allocationSize 50).
CREATE SEQUENCE roles_seq INCREMENT BY 50 START WITH 1 MINVALUE 1;
CREATE SEQUENCE confirmation_otps_seq INCREMENT BY 50 START WITH 1 MINVALUE 1;

-- roles
CREATE TABLE roles (
    id   INTEGER      NOT NULL DEFAULT nextval('roles_seq'),
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

-- users
CREATE TABLE users (
    id         UUID         NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    phone      VARCHAR(255) NOT NULL,
    image_key  VARCHAR(255),
    active     BOOLEAN      NOT NULL,
    enabled    BOOLEAN      NOT NULL,
    role_id    INTEGER,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- products
CREATE TABLE products (
    id                  UUID           NOT NULL,
    name                VARCHAR(255)   NOT NULL,
    sku                 VARCHAR(255)   NOT NULL,
    description         TEXT,
    selling_price       NUMERIC(19, 4) NOT NULL,
    cost_price          NUMERIC(19, 4) NOT NULL,
    quantity            INTEGER        NOT NULL,
    low_stock_threshold INTEGER        NOT NULL,
    active              BOOLEAN        NOT NULL,
    created_at          TIMESTAMP(6)   NOT NULL,
    updated_at          TIMESTAMP(6)   NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uq_products_sku UNIQUE (sku)
);

-- categories
CREATE TABLE categories (
    id         UUID         NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uq_categories_name UNIQUE (name)
);

-- product_categories: join table for Category <-> Product (@ManyToMany)
CREATE TABLE product_categories (
    categories_id UUID NOT NULL,
    product_id    UUID NOT NULL,
    CONSTRAINT pk_product_categories PRIMARY KEY (categories_id, product_id),
    CONSTRAINT fk_product_categories_category FOREIGN KEY (categories_id) REFERENCES categories (id),
    CONSTRAINT fk_product_categories_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- product_images: @ElementCollection of image keys for a product
CREATE TABLE product_images (
    product_id UUID NOT NULL,
    image_key  VARCHAR(255),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- sales
CREATE TABLE sales (
    id         UUID             NOT NULL,
    quantity   INTEGER          NOT NULL,
    sale_price DOUBLE PRECISION NOT NULL,
    sold_by_id UUID             NOT NULL,
    product_id UUID             NOT NULL,
    timestamp  TIMESTAMP(6),
    CONSTRAINT pk_sales PRIMARY KEY (id),
    CONSTRAINT fk_sales_user FOREIGN KEY (sold_by_id) REFERENCES users (id),
    CONSTRAINT fk_sales_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- restocks
CREATE TABLE restocks (
    id              UUID    NOT NULL,
    quantity        INTEGER NOT NULL,
    restocked_by_id UUID    NOT NULL,
    product_id      UUID    NOT NULL,
    timestamp       TIMESTAMP(6),
    CONSTRAINT pk_restocks PRIMARY KEY (id),
    CONSTRAINT fk_restocks_user FOREIGN KEY (restocked_by_id) REFERENCES users (id),
    CONSTRAINT fk_restocks_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- refresh_tokens
CREATE TABLE refresh_tokens (
    id         UUID NOT NULL,
    user_id    UUID,
    created_at TIMESTAMP(6),
    token      TEXT,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- confirmation_otps
CREATE TABLE confirmation_otps (
    id         INTEGER      NOT NULL DEFAULT nextval('confirmation_otps_seq'),
    otp        VARCHAR(255) NOT NULL,
    confirmed  BOOLEAN      NOT NULL,
    user_id    UUID,
    created_at TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_confirmation_otps PRIMARY KEY (id),
    CONSTRAINT fk_confirmation_otps_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Helper indexes on foreign keys used for lookups.
CREATE INDEX idx_users_role ON users (role_id);
CREATE INDEX idx_product_categories_product ON product_categories (product_id);
CREATE INDEX idx_product_images_product ON product_images (product_id);
CREATE INDEX idx_sales_user ON sales (sold_by_id);
CREATE INDEX idx_sales_product ON sales (product_id);
CREATE INDEX idx_restocks_user ON restocks (restocked_by_id);
CREATE INDEX idx_restocks_product ON restocks (product_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_confirmation_otps_user ON confirmation_otps (user_id);
