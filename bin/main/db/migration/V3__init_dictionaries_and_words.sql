CREATE TABLE dictionaries (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL REFERENCES languages(code) ON DELETE CASCADE
);

CREATE TABLE words (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dictionary_id UUID NOT NULL REFERENCES dictionaries(id) ON DELETE CASCADE,
    word          VARCHAR(255) NOT NULL,
    definitions   TEXT[] NOT NULL,
    examples      TEXT[],
    synonyms_id   TEXT[],
    antonyms_id   TEXT[],
    scores        INT NOT NULL DEFAULT 0,
    status        VARCHAR(20) NOT NULL DEFAULT 'SEED',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
