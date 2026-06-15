CREATE TABLE cart (
    cart_id       UUID           NOT NULL,
    user_id       UUID           NOT NULL,
    restaurant_id UUID           NOT NULL,
    total         DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at    TIMESTAMP      NOT NULL,
    updated_at    TIMESTAMP      NOT NULL,
    CONSTRAINT pk_cart               PRIMARY KEY (cart_id),
    CONSTRAINT uq_cart_user          UNIQUE (user_id),
    CONSTRAINT fk_cart_user          FOREIGN KEY (user_id)       REFERENCES users (user_id),
    CONSTRAINT fk_cart_restaurant    FOREIGN KEY (restaurant_id) REFERENCES restaurant (restaurant_id)
);
