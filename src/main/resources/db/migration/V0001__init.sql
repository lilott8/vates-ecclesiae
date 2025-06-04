CREATE TABLE corpus (
    id SERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    url_hash varchar(1024) NOT NULL,
    domain VARCHAR(2048) NOT NULL,
    page_title VARCHAR(2048) NOT NULL,
    document_path VARCHAR(1024) NOT NULL,
    document_hash VARCHAR(1024) NOT NULL,
    times_crawled INTEGER DEFAULT 0,
    state VARCHAR(255) NOT NULL,
    first_crawled TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_corpus_domain ON corpus(domain);
CREATE INDEX idx_corpus_document_hash ON corpus(document_hash);
CREATE INDEX idx_corpus_state ON corpus(state);
CREATE INDEX idx_corpus_url_hash ON corpus(url_hash);

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