#align(center)[
  #text(size: 14pt, weight: "bold")[2 этап]
]

#v(0.5cm)

*1. ER-модель*

#image("./images/er.png", width: 110%)

#pagebreak()

*2. Даталогическая модель*

#image("./images/datalogic.png", width: 100%)

*3. Реализация даталогической модели и триггеров*

```sql
CREATE TABLE USER_ACCOUNT (
    id SERIAL PRIMARY KEY,
    yandex_id VARCHAR UNIQUE, 
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

CREATE TABLE DREAM_RECORD (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES USER_ACCOUNT(id) ON DELETE CASCADE,
    category_id INT REFERENCES CATEGORY(id) ON DELETE SET NULL,
    visualization_id INT REFERENCES VISUALIZATION(id) ON DELETE SET NULL,
    title VARCHAR NOT NULL,
    content TEXT NOT NULL,
    privacy VARCHAR NOT NULL CHECK (privacy IN ('PUBLIC','PRIVATE')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE LOT (
    id SERIAL PRIMARY KEY,
    dream_record_id INT REFERENCES DREAM_RECORD(id) ON DELETE SET NULL,
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
    type VARCHAR NOT NULL CHECK (type IN ('INFO', 'ALERT', 'TRANSACTION')),
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
    admin_id INT NOT NULL REFERENCES USER_ACCOUNT(id),
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

\

*4. Скрипты для создания, удаления базы данных, заполнения базы тестовыми данными*

*Скрипт для создания БД:*

```sql
CREATE DATABASE IF NOT EXISTS dream_market;
\c dream_market;
```

\

*Скрипт для удаления БД:*

```sql
DROP DATABASE IF EXISTS dream_market;
```

\

*Скрипт для заполнения БД тестовыми данными:*

```sql
\c dream_market;

INSERT INTO USER_ACCOUNT (yandex_id, username, email, role, balance)
VALUES
('ya_001', 'vladislav', 'vladislav@example.com', 'ADMIN', 1500.00),
('ya_002', 'dreamer1', 'dreamer1@example.com', 'USER', 300.00),
('ya_003', 'noctis', 'noctis@example.com', 'USER', 0.00);

INSERT INTO CATEGORY (name)
VALUES
('Lucid Dreams'),
('Nightmares'),
('Adventure'),
('Abstract'),
('Fantasy');

INSERT INTO TAG (name)
VALUES
('Flying'),
('Water'),
('Mountains'),
('Fear'),
('Light'),
('AI-generated');

INSERT INTO DREAM_RECORD (user_id, category_id, visualization_id, title, content, privacy)
VALUES
(1, 1, 1, 'Полёт над Исаакием', 'Лечу над городом, вижу купола и солнце отражается в воде.', 'PUBLIC'),
(2, 3, 2, 'Пробежка по светящемуся лесу', 'Бегу между огромных грибов, всё светится вокруг.', 'PUBLIC'),
(2, 2, NULL, 'Кошмар с зеркалами', 'Каждое отражение показывает другую версию меня.', 'PRIVATE'),
(3, 4, 3, 'Голубой туман', 'Стою на берегу моря, а вокруг только густой голубой туман.', 'PUBLIC'),
(1, 5, 4, 'Тихая улица', 'Я шёл по улице, где никто не жил, но каждое окно светилось.', 'PRIVATE');

INSERT INTO VISUALIZATION (prompt, generator, file_path, mime, width, height, duration, status)
VALUES
('A bright sky above Saint Petersburg cathedral', 'StableDiffusionXL', '/s3/visuals/isaakiy.png', 'image/png', 1024, 768, NULL, 'READY'),
('Dream forest with glowing mushrooms', 'RunwayML', '/s3/visuals/forest.mp4', 'video/mp4', 1920, 1080, 20, 'ACCEPTED'),
('A surreal blue fog landscape', 'Midjourney', '/s3/visuals/blue_fog.png', 'image/png', 512, 512, NULL, 'REJECTED'),
('Empty night street', 'StableDiffusionXL', '/s3/visuals/street.png', 'image/png', 1024, 768, NULL, 'PENDING');

INSERT INTO DREAM_RECORD_TAG (dream_record_id, tag_id)
VALUES
(1, 1),
(1, 5),
(2, 2),
(2, 6),
(3, 4),
(4, 3),
(5, 5);

INSERT INTO LOT (dream_record_id, title, description, price, status, submitted_at, reviewed_at)
VALUES
(1, 'Картина "Полёт над Исаакием"', 'Постер с визуализацией сна Владислава.', 1200.00, 'OPEN', now(), NULL),
(2, 'Видеосон "Светящийся лес"', 'Анимация из сновидения о волшебном лесу.', 2000.00, 'OPEN', now(), NULL),
(4, 'Изображение "Голубой туман"', 'AI-визуализация сна о море и тумане.', 900.00, 'CLOSED', now() - interval '5 days', now() - interval '3 days');

INSERT INTO TRANSACTION (lot_id, buyer_id, seller_id, fee, status)
VALUES
(3, 2, 1, 45.00, 'COMPLETED'),
(1, 3, 1, 60.00, 'PENDING');

INSERT INTO COMMENT (lot_id, user_id, body)
VALUES
(1, 2, 'Очень атмосферное изображение! Хотел бы повесить у себя.'),
(2, 3, 'Видео получилось невероятным.'),
(3, 2, 'Туман как будто живой.');

INSERT INTO RATING (lot_id, user_id, score)
VALUES
(1, 2, 5),
(2, 3, 4),
(3, 2, 5);

INSERT INTO NOTIFICATION (user_id, type, reference_type, reference_id, title, message)
VALUES
(1, 'INFO', 'TRANSACTION', 1, 'Продажа завершена', 'Вы успешно продали лот №3.'),
(2, 'ALERT', 'LOT', 1, 'Новый комментарий', 'Ваш лот получил комментарий.'),
(3, 'TRANSACTION', 'TRANSACTION', 2, 'Оплата ожидает подтверждения', 'Ваша покупка ожидает одобрения.');

INSERT INTO MODERATION_LOG (lot_id, admin_id, action, reason)
VALUES
(3, 1, 'REVIEWED', 'Контент соответствует правилам.'),
(2, 1, 'REJECTED', 'Обнаружены элементы, нарушающие политику контента.');
```

#v(0.5cm)

*5. Pl/pgsql-функции и процедуры, для выполнения критически важных запросов*
```sql
CREATE OR REPLACE FUNCTION proc_archive_lot(p_lot_id INT, p_admin_id INT, p_reason TEXT)
RETURNS VOID AS $$
BEGIN
    INSERT INTO moderation_log (lot_id, user_id, action, reason, created_at)
    VALUES (p_lot_id, p_admin_id, 'ARCHIVE', p_reason, now());

    UPDATE lot SET status = 'CLOSED', moderation_reason = p_reason, reviewed_at = now()
    WHERE id = p_lot_id;
END;
$$ LANGUAGE plpgsql;
```
Процедура предназначена для архивирования (закрытия) лота администратором. Она используется, когда объявление о продаже или публикация больше неактуальна, нарушает правила или требует удаления с площадки.

\

*6. Индексы*

```sql
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_user_yandex_id
ON user_account (yandex_id);
```
Почему: при входе через Яндекс ID нужно быстро найти локальную запись. 

\

```sql
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_dream_record_user_created_at
ON dream_record (user_id, created_at DESC);
```

Почему: покрывает запросы на получение коллекций снов с пагинацией/сортировкой по дате, снижает время выдачи списка.

\

```sql
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_visualization_status_created
ON visualization (status, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_visualization_dream_record
ON visualization (dream_record_id);
```

Почему: менеджер задач будет фильтровать по status и брать задачи → индекс ускорит выборку. Индекс по dream_record_id ускоряет join visualization → dream_record.

\

```sql
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_lot_status_price
ON lot (status, price);

CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_lot_status_submitted
ON lot (status, submitted_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_lot_visualization
ON lot (visualization_id);
```

Почему: частые операции, такие как показать публичные лоты, фильтровать по статусу, цене, сортировать.

\

```sql
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_notification_user_isread_created
ON notification (user_id, is_read, created_at DESC);
```

Почему: ускоряет выдачу списка уведомлений и подсчёт непрочитанных.

\

```sql
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_comment_lot_created
ON comment (lot_id, created_at DESC);

CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_rating_lot_user
ON rating (lot_id, user_id);
```

Почему: ускоряет отображение комментариев на странице лота и предотвращает множественные оценки от одного пользователя.

\

```sql
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_drtag_tag
ON dream_record_tag (tag_id, dream_record_id);
```

Почему: при фильтрации каталога по тегу быстро найти соответствующие записи.
