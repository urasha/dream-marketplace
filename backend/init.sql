-- ===============================================
-- 1. СОЗДАНИЕ ТАБЛИЦ (Schema Definition)
-- ===============================================

-- 1.1. Таблица пользователей
CREATE TABLE USER_ACCOUNT (
                              id SERIAL PRIMARY KEY,
                              yandex_id VARCHAR UNIQUE,
                              username VARCHAR NOT NULL,
                              email VARCHAR NOT NULL UNIQUE,
                              role VARCHAR NOT NULL CHECK (role IN ('USER','ADMIN')),
                              balance NUMERIC DEFAULT 0 CHECK (balance >= 0),
                              created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 1.2. Справочник категорий
CREATE TABLE CATEGORY (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR NOT NULL UNIQUE
);

-- 1.3. Справочник тегов
CREATE TABLE TAG (
                     id SERIAL PRIMARY KEY,
                     name VARCHAR NOT NULL UNIQUE
);

-- 1.4. Таблица визуализаций
CREATE TABLE VISUALIZATION (
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

-- 1.5. Таблица записей снов
CREATE TABLE DREAM_RECORD (
                              id SERIAL PRIMARY KEY,
                              user_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE CASCADE,
                              category_id INT REFERENCES CATEGORY(id),
                              visualization_id INT UNIQUE REFERENCES VISUALIZATION(id) ON DELETE SET NULL,
                              title VARCHAR NOT NULL,
                              content TEXT NOT NULL,
                              privacy VARCHAR NOT NULL CHECK (privacy IN ('PUBLIC','PRIVATE')),
                              created_at TIMESTAMP NOT NULL DEFAULT now(),
                              updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 1.6. Связующая таблица для DreamRecord и Tag (Many-to-Many)
CREATE TABLE DREAM_RECORD_TAG (
                                  dream_record_id INT NOT NULL REFERENCES DREAM_RECORD(id) ON DELETE CASCADE,
                                  tag_id INT NOT NULL REFERENCES TAG(id) ON DELETE CASCADE,
                                  PRIMARY KEY (dream_record_id, tag_id)
);

-- 1.7. Таблица лотов (товаров)
CREATE TABLE LOT (
                     id SERIAL PRIMARY KEY,
                     dream_record_id INT UNIQUE NOT NULL REFERENCES DREAM_RECORD(id) ON DELETE RESTRICT,
                     title VARCHAR NOT NULL,
                     description TEXT,
                     price NUMERIC(10, 2) NOT NULL CHECK (price > 0),
                     status VARCHAR NOT NULL CHECK (status IN ('OPEN','SOLD','CLOSED')),
                     submitted_at TIMESTAMP NOT NULL DEFAULT now(),
                     reviewed_at TIMESTAMP,
                     moderation_reason TEXT
);

-- 1.8. Таблица транзакций
CREATE TABLE TRANSACTION (
                             id SERIAL PRIMARY KEY,
                             lot_id INT UNIQUE NOT NULL REFERENCES LOT(id) ON DELETE RESTRICT,
                             buyer_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE RESTRICT,
                             seller_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE RESTRICT,
                             amount NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
                             fee NUMERIC(10, 2) NOT NULL CHECK (fee >= 0),
                             transaction_date TIMESTAMP NOT NULL DEFAULT now()
);

-- 1.9. Таблица комментариев
CREATE TABLE COMMENT (
                         id SERIAL PRIMARY KEY,
                         lot_id INT NOT NULL REFERENCES LOT(id) ON DELETE CASCADE,
                         user_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE CASCADE,
                         content TEXT NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 1.10. Таблица рейтингов
CREATE TABLE RATING (
                        id SERIAL PRIMARY KEY,
                        lot_id INT NOT NULL REFERENCES LOT(id) ON DELETE CASCADE,
                        user_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE CASCADE,
                        value INT NOT NULL CHECK (value BETWEEN 1 AND 5),
                        created_at TIMESTAMP NOT NULL DEFAULT now(),
                        UNIQUE (lot_id, user_id)
);

-- 1.11. Таблица уведомлений
CREATE TABLE NOTIFICATION (
                              id SERIAL PRIMARY KEY,
                              user_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE CASCADE,
                              message TEXT NOT NULL,
                              is_read BOOLEAN NOT NULL DEFAULT FALSE,
                              created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 1.12. Таблица логов модерации
CREATE TABLE MODERATION_LOG (
                                id SERIAL PRIMARY KEY,
                                lot_id INT NOT NULL REFERENCES LOT(id) ON DELETE CASCADE,
                                admin_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE RESTRICT,
                                action VARCHAR NOT NULL, -- 'ARCHIVE', 'REJECT', 'APPROVE'
                                reason TEXT,
                                created_at TIMESTAMP NOT NULL DEFAULT now()
);


-- ===============================================
-- 2. PL/pgSQL ЛОГИКА (Функции, Процедуры и Триггеры)
-- ===============================================

-- 2.1. Функция для обновления поля updated_at (trg_update_updated_at)
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для автоматического обновления поля DREAM_RECORD.updated_at
CREATE TRIGGER trg_update_updated_at
    BEFORE UPDATE ON DREAM_RECORD
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();


-- 2.2. Функция для проверки, что покупатель и продавец не одно лицо (trg_check_buyer_seller)
CREATE OR REPLACE FUNCTION check_buyer_seller()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.buyer_id = NEW.seller_id THEN
        RAISE EXCEPTION 'Покупатель не может быть продавцом в одной транзакции.';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для проверки перед созданием транзакции
CREATE TRIGGER trg_check_buyer_seller
    BEFORE INSERT ON TRANSACTION
    FOR EACH ROW
    EXECUTE FUNCTION check_buyer_seller();


-- 2.3. Процедура для архивации лота (proc_archive_lot)
-- Эта процедура вызывается из Java-кода (LotRepository.archiveLot)
CREATE OR REPLACE PROCEDURE proc_archive_lot(
    lot_id_in INT,
    admin_id_in INT,
    reason_in TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- 1. Обновляем статус лота
UPDATE LOT
SET
    status = 'CLOSED',
    reviewed_at = now(),
    moderation_reason = reason_in
WHERE id = lot_id_in;

-- 2. Записываем действие модерации в лог
INSERT INTO MODERATION_LOG (lot_id, admin_id, action, reason, created_at)
VALUES (lot_id_in, admin_id_in, 'ARCHIVE', reason_in, now());

END;
$$;

-- ===============================================
-- 3. ИНДЕКСЫ (Оптимизация запросов)
-- ===============================================

-- 3.1. Индекс для сортировки снов пользователя по дате (FR10)
CREATE INDEX ix_dream_record_user_created_at
    ON DREAM_RECORD (user_id, created_at DESC);

-- 3.2. Индекс для поиска открытых лотов по цене
CREATE INDEX ix_lot_status_price
    ON LOT (status, price);

-- 3.3. Индекс для оптимизации выборки уведомлений
CREATE INDEX ix_notification_user_read_created_at
    ON NOTIFICATION (user_id, is_read, created_at DESC);

-- 3.4. Индекс для оптимизации выборки комментариев к лоту
CREATE INDEX ix_comment_lot_created_at
    ON COMMENT (lot_id, created_at DESC);