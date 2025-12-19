-- Initial schema derived from init.sql and docs/second-stage.typ

-- Users
CREATE TABLE IF NOT EXISTS user_account (
    id SERIAL PRIMARY KEY,
    yandex_id VARCHAR UNIQUE,
    username VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    role VARCHAR NOT NULL CHECK (role IN ('USER','ADMIN')),
    balance NUMERIC DEFAULT 0 CHECK (balance >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Category and tag
CREATE TABLE IF NOT EXISTS category (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tag (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE
);

-- Visualization
CREATE TABLE IF NOT EXISTS visualization (
    id SERIAL PRIMARY KEY,
    prompt VARCHAR,
    generator VARCHAR,
    file_path VARCHAR,
    mime VARCHAR,
    width INT CHECK (width >= 0),
    height INT CHECK (height >= 0),
    duration INT CHECK (duration >= 0),
    status VARCHAR CHECK (status IN ('PENDING','READY','FAILED','ACCEPTED','REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Dreams
CREATE TABLE IF NOT EXISTS dream_record (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    category_id INT REFERENCES category(id),
    visualization_id INT UNIQUE REFERENCES visualization(id) ON DELETE SET NULL,
    title VARCHAR NOT NULL,
    content TEXT NOT NULL,
    privacy VARCHAR NOT NULL CHECK (privacy IN ('PUBLIC','PRIVATE')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS dream_record_tag (
    dream_record_id INT NOT NULL REFERENCES dream_record(id) ON DELETE CASCADE,
    tag_id INT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (dream_record_id, tag_id)
);

-- Lots
CREATE TABLE IF NOT EXISTS lot (
    id SERIAL PRIMARY KEY,
    dream_record_id INT UNIQUE NOT NULL REFERENCES dream_record(id) ON DELETE RESTRICT,
    title VARCHAR NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL CHECK (price > 0),
    status VARCHAR NOT NULL CHECK (status IN ('OPEN','SOLD','CLOSED')),
    submitted_at TIMESTAMP NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMP,
    moderation_reason TEXT
);

-- Transactions
CREATE TABLE IF NOT EXISTS transaction (
    id SERIAL PRIMARY KEY,
    lot_id INT UNIQUE NOT NULL REFERENCES lot(id) ON DELETE RESTRICT,
    buyer_id INT NOT NULL REFERENCES user_account(id) ON DELETE RESTRICT,
    seller_id INT NOT NULL REFERENCES user_account(id) ON DELETE RESTRICT,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    fee NUMERIC(10, 2) NOT NULL CHECK (fee >= 0),
    transaction_date TIMESTAMP NOT NULL DEFAULT now()
);

-- Comments
CREATE TABLE IF NOT EXISTS comment (
    id SERIAL PRIMARY KEY,
    lot_id INT NOT NULL REFERENCES lot(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Ratings
CREATE TABLE IF NOT EXISTS rating (
    id SERIAL PRIMARY KEY,
    lot_id INT NOT NULL REFERENCES lot(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    value INT NOT NULL CHECK (value BETWEEN 1 AND 5),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (lot_id, user_id)
);

-- Notifications
CREATE TABLE IF NOT EXISTS notification (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Moderation logs
CREATE TABLE IF NOT EXISTS moderation_log (
    id SERIAL PRIMARY KEY,
    lot_id INT NOT NULL REFERENCES lot(id) ON DELETE CASCADE,
    admin_id INT NOT NULL REFERENCES user_account(id) ON DELETE RESTRICT,
    action VARCHAR NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Trigger: update updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_updated_at
    BEFORE UPDATE ON dream_record
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

-- Trigger: buyer != seller
CREATE OR REPLACE FUNCTION check_buyer_seller()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.buyer_id = NEW.seller_id THEN
        RAISE EXCEPTION 'Buyer cannot be the seller in the same transaction';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_buyer_seller
    BEFORE INSERT ON transaction
    FOR EACH ROW
    EXECUTE FUNCTION check_buyer_seller();

CREATE OR REPLACE PROCEDURE proc_archive_lot(
    lot_id_in INT,
    admin_id_in INT,
    reason_in TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE lot
    SET status = 'CLOSED',
        reviewed_at = now(),
        moderation_reason = reason_in
    WHERE id = lot_id_in;

    INSERT INTO moderation_log (lot_id, admin_id, action, reason, created_at)
    VALUES (lot_id_in, admin_id_in, 'ARCHIVE', reason_in, now());
END;
$$;

CREATE INDEX IF NOT EXISTS ix_dream_record_user_created_at ON dream_record (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_visualization_status_created ON visualization (status, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_lot_status_price ON lot (status, price);
CREATE INDEX IF NOT EXISTS ix_lot_status_submitted ON lot (status, submitted_at DESC);
CREATE INDEX IF NOT EXISTS ix_notification_user_read_created_at ON notification (user_id, is_read, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_comment_lot_created_at ON comment (lot_id, created_at DESC);
