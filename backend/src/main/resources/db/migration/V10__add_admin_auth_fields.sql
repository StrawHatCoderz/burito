ALTER TABLE users ADD COLUMN role VARCHAR(255) DEFAULT 'USER';
UPDATE users SET role = 'USER' WHERE role IS NULL;

ALTER TABLE restaurant ADD COLUMN owner_id UUID;
ALTER TABLE restaurant ADD CONSTRAINT fk_restaurant_owner FOREIGN KEY (owner_id) REFERENCES users(user_id);
