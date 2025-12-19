-- Add link from visualization to dream_record for variant tracking
ALTER TABLE visualization
    ADD COLUMN IF NOT EXISTS dream_record_id INT REFERENCES dream_record(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS ix_visualization_dream_record ON visualization (dream_record_id, created_at DESC);
