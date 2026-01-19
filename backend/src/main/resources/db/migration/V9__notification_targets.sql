ALTER TABLE notification
    ADD COLUMN IF NOT EXISTS target_lot_id INT,
    ADD COLUMN IF NOT EXISTS target_comment_id INT;
