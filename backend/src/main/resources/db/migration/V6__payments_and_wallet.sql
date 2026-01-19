-- Payment intents for YooKassa deposits and updated balance flow

CREATE TABLE IF NOT EXISTS payment_intent (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    provider_payment_id VARCHAR(128) NOT NULL,
    confirmation_url TEXT NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING', 'SUCCEEDED', 'CANCELED', 'FAILED')),
    credited BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_payment_intent_provider_id ON payment_intent(provider_payment_id);
CREATE INDEX IF NOT EXISTS ix_payment_intent_user_status ON payment_intent(user_id, status);

-- keep updated_at in sync
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_payment_intent_updated') THEN
        CREATE TRIGGER trg_payment_intent_updated
            BEFORE UPDATE ON payment_intent
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at();
    END IF;
END $$;
