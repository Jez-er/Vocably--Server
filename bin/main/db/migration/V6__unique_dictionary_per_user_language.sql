-- One dictionary per user per language. The code already assumed this (dictionaries were looked up
-- by user + language code expecting at most one row), but nothing enforced it, so concurrent
-- creates could leave duplicates behind.
ALTER TABLE dictionaries
    ADD CONSTRAINT uq_dictionaries_user_language UNIQUE (user_id, language_code);
