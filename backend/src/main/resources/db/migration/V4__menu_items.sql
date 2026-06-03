CREATE TABLE menu_item (
    menu_item_id  UUID           NOT NULL,
    name          VARCHAR(255)   NOT NULL,
    description   VARCHAR(500),
    price         DECIMAL(10, 2) NOT NULL,
    category      VARCHAR(50)    NOT NULL,
    is_available  BOOLEAN        NOT NULL DEFAULT true,
    restaurant_id UUID           NOT NULL,
    CONSTRAINT pk_menu_item            PRIMARY KEY (menu_item_id),
    CONSTRAINT fk_menu_item_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurant (restaurant_id) ON DELETE CASCADE
);
