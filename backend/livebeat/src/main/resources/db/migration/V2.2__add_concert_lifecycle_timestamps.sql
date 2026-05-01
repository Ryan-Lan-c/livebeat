ALTER TABLE concert.concerts
    ADD COLUMN cancelled_at TIMESTAMPTZ,
    ADD COLUMN ended_at     TIMESTAMPTZ;

COMMENT ON COLUMN concert.concerts.cancelled_at IS 'Timestamp when the concert was cancelled; NULL if not cancelled | 演唱會取消時間，未取消為 NULL';
COMMENT ON COLUMN concert.concerts.ended_at     IS 'Timestamp when the concert was marked as ended; NULL if not ended | 演唱會結束時間，未結束為 NULL';
