ALTER TABLE users ADD COLUMN phone_number VARCHAR(255);
ALTER TABLE users ADD COLUMN address_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_users_address FOREIGN KEY (address_id) REFERENCES address (address_id);
