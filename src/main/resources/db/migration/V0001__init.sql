CREATE TABLE corpus (
    id SERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    domain VARCHAR(2048) NOT NULL,
    path VARCHAR(1024) NOT NULL,
    page_title VARCHAR(2048) NOT NULL,
    hash VARCHAR(1024) NOT NULL,
    times_crawled INTEGER DEFAULT 0,
    state VARCHAR(255) NOT NULL,
    first_crawled TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_corpus_domain ON corpus(domain);
CREATE INDEX idx_corpus_hash ON corpus(hash);
CREATE INDEX idx_corpus_state on corpus(state);

-- Function to update last_updated
CREATE OR REPLACE FUNCTION update_last_updated_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.last_updated = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to call the function before each UPDATE
CREATE TRIGGER trg_update_last_updated
BEFORE UPDATE ON corpus
FOR EACH ROW
EXECUTE FUNCTION update_last_updated_column();