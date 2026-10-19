ALTER TABLE words ADD COLUMN last_learned_at TIMESTAMPTZ;

CREATE INDEX idx_words_dictionary_id_status ON words (dictionary_id, status);
