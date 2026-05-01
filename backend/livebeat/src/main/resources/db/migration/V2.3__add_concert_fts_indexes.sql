-- Enable pg_trgm for trigram-based similarity search (ILIKE with GIN index + similarity() ranking)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- GIN trigram indexes on searchable text columns
CREATE INDEX IF NOT EXISTS idx_concerts_title_trgm  ON concert.concerts USING GIN (title  gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_concerts_artist_trgm ON concert.concerts USING GIN (artist gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_concerts_venue_trgm  ON concert.concerts USING GIN (venue  gin_trgm_ops);
