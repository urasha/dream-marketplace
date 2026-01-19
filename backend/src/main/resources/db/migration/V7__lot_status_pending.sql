ALTER TABLE lot
    DROP CONSTRAINT IF EXISTS lot_status_check;

ALTER TABLE lot
    ADD CONSTRAINT lot_status_check
    CHECK (status IN ('OPEN', 'SOLD', 'CLOSED', 'PENDING'));