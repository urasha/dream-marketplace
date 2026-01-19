CREATE TABLE IF NOT EXISTS platform_wallet (
    id BIGINT PRIMARY KEY,
    balance NUMERIC(12, 2) NOT NULL DEFAULT 0
);

INSERT INTO platform_wallet (id, balance)
VALUES (1, 0)
ON CONFLICT (id) DO NOTHING;