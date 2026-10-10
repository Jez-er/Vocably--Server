CREATE INDEX idx_words_dictionary_id ON words (dictionary_id);

CREATE INDEX idx_words_lower_word ON words (lower(word));

CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens (expires_at);
