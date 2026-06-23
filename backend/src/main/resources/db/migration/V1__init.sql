CREATE TABLE address (
    address_id      BIGSERIAL    NOT NULL,
    street  VARCHAR(255) NOT NULL,
    city    VARCHAR(255) NOT NULL,
    state   VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    zipcode VARCHAR(255) NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (address_id)
);

CREATE TABLE restaurant (
    restaurant_id        UUID         NOT NULL,
    restaurant_name      VARCHAR(255),
    description          VARCHAR(255),
    cuisine_type         VARCHAR(255),
    rating               DOUBLE PRECISION NOT NULL,
    est_delivery_minutes DOUBLE PRECISION NOT NULL,
    is_open              BOOLEAN          NOT NULL,
    created_at           TIMESTAMP,
    address_id                   BIGINT,
    CONSTRAINT pk_restaurant         PRIMARY KEY (restaurant_id),
    CONSTRAINT fk_restaurant_address FOREIGN KEY (address_id) REFERENCES address (address_id)
);

CREATE TABLE users (
    user_id       UUID         NOT NULL,
    email         VARCHAR(255) NOT NULL,
    hash_password VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL,
    CONSTRAINT pk_users       PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email)
);
