ALTER TABLE dictionaries
    ADD CONSTRAINT uq_dictionaries_user_language UNIQUE (user_id, language_code);
