-- Drop NOT NULL constraint on user_id
ALTER TABLE cart ALTER COLUMN user_id DROP NOT NULL;

-- Add guest_id column
ALTER TABLE cart ADD COLUMN guest_id UUID;

-- Add unique constraint on guest_id
ALTER TABLE cart ADD CONSTRAINT uq_cart_guest UNIQUE (guest_id);

-- Add check constraint to ensure at least one owner identifier is present
ALTER TABLE cart ADD CONSTRAINT chk_cart_owner CHECK (user_id IS NOT NULL OR guest_id IS NOT NULL);

-- Add status column
ALTER TABLE cart ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
