CREATE TABLE cart_item (
    cart_item_id UUID           NOT NULL,
    cart_id      UUID           NOT NULL,
    menu_item_id UUID           NOT NULL,
    quantity     INTEGER        NOT NULL CHECK (quantity >= 1),
    unit_price   DECIMAL(10, 2) NOT NULL,
    subtotal     DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_cart_item               PRIMARY KEY (cart_item_id),
    CONSTRAINT fk_cart_item_cart          FOREIGN KEY (cart_id)      REFERENCES cart (cart_id)      ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_menu_item     FOREIGN KEY (menu_item_id) REFERENCES menu_item (menu_item_id)
);
