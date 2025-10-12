#align(center)[
  #text(size: 14pt, weight: "bold")[2 этап]
]

#v(0.5cm)

*1. ER-модель*

#image("./er.png", width: 110%)

#pagebreak()

*2. Даталогическая модель*

#image("./datalogic.png", width: 100%)

*3. Реализация даталогической модели и триггеров*

```sql
CREATE TABLE USER_ACCOUNT (
    id SERIAL PRIMARY KEY,
    username VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    role VARCHAR NOT NULL CHECK (role IN ('USER','ADMIN')),
    balance NUMERIC DEFAULT 0 CHECK (balance >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE CATEGORY (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE TAG (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE DREAM_RECORD (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE CASCADE,
    category_id INT REFERENCES CATEGORY(id) ON DELETE SET NULL,
    title VARCHAR NOT NULL,
    content TEXT NOT NULL,
    privacy VARCHAR NOT NULL CHECK (privacy IN ('PUBLIC','PRIVATE')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE VISUALIZATION (
    id SERIAL PRIMARY KEY,
    dream_record_id INT NOT NULL REFERENCES DREAM_RECORD(id) ON DELETE CASCADE,
    generator VARCHAR,
    file_path VARCHAR,
    mime VARCHAR,
    width INT CHECK (width >= 0),
    height INT CHECK (height >= 0),
    duration INT CHECK (duration >= 0),
    status VARCHAR CHECK (status IN ('PENDING','READY','FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE LOT (
    id SERIAL PRIMARY KEY,
    visualization_id INT REFERENCES VISUALIZATION(id) ON DELETE SET NULL,
    title VARCHAR NOT NULL,
    description TEXT,
    price NUMERIC CHECK (price >= 0),
    status VARCHAR CHECK (status IN ('OPEN','SOLD','CLOSED')),
    submitted_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    moderation_reason TEXT
);

CREATE TABLE TRANSACTION (
    id SERIAL PRIMARY KEY,
    lot_id INT NOT NULL REFERENCES LOT(id) ON DELETE CASCADE,
    buyer_id INT NOT NULL REFERENCES USER_ACCOUNT(id),
    seller_id INT NOT NULL REFERENCES USER_ACCOUNT(id),
    amount NUMERIC NOT NULL CHECK (amount > 0),
    fee NUMERIC DEFAULT 0 CHECK (fee >= 0),
    status VARCHAR CHECK (status IN ('PENDING','COMPLETED','FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE COMMENT (
    id SERIAL PRIMARY KEY,
    lot_id INT NOT NULL REFERENCES LOT(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES USER_ACCOUNT(id),
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE RATING (
    id SERIAL PRIMARY KEY,
    lot_id INT NOT NULL REFERENCES LOT(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES USER_ACCOUNT(id),
    score INT NOT NULL CHECK (score >= 1 AND score <= 5),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE NOTIFICATION (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES USER_ACCOUNT(id),
    type VARCHAR NOT NULL CHECK (type IN ('INFO', 'ALERT', 'TRANSACTION'));
    reference_type VARCHAR CHECK (
        reference_type IN (
            'LOT',
            'TRANSACTION',
            'DREAM_RECORD',
            'COMMENT',
            'USER',
            'MODERATION_LOG',
            'SYSTEM'
        )
    ),
    reference_id INT,
    title VARCHAR,
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE MODERATION_LOG (
    id SERIAL PRIMARY KEY,
    lot_id INT NOT NULL REFERENCES LOT(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES USER_ACCOUNT(id),
    action VARCHAR NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE DREAM_RECORD_TAG (
    dream_record_id INT NOT NULL REFERENCES DREAM_RECORD(id) ON DELETE CASCADE,
    tag_id INT NOT NULL REFERENCES TAG(id) ON DELETE CASCADE,
    PRIMARY KEY(dream_record_id, tag_id)
);

-- Triggers --

-- Auto-update 'updated_at' in DREAM_RECORD
CREATE OR REPLACE FUNCTION trg_update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_dream_record_update
BEFORE UPDATE ON DREAM_RECORD
FOR EACH ROW
EXECUTE FUNCTION trg_update_updated_at();

-- Checking the 'buyer_id' ≠ 'seller_id' in the TRANSACTION
CREATE OR REPLACE FUNCTION trg_check_buyer_seller()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.buyer_id = NEW.seller_id THEN
        RAISE EXCEPTION 'Buyer and seller cannot be the same user';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_transaction_check
BEFORE INSERT OR UPDATE ON TRANSACTION
FOR EACH ROW
EXECUTE FUNCTION trg_check_buyer_seller();
```