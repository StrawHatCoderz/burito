ALTER TABLE cart DROP CONSTRAINT IF EXISTS uq_cart_guest;
ALTER TABLE cart DROP CONSTRAINT IF EXISTS uq_cart_user;

CREATE UNIQUE INDEX idx_cart_user_pending ON cart (user_id) WHERE status = 'PENDING';
CREATE UNIQUE INDEX idx_cart_guest_pending ON cart (guest_id) WHERE status = 'PENDING';
