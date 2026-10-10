ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE users
    ADD COLUMN provider    VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN provider_id VARCHAR(255);

ALTER TABLE users
    ADD CONSTRAINT uq_users_provider_identity UNIQUE (provider, provider_id);

ALTER TABLE users
    ADD CONSTRAINT ck_users_credentials CHECK (
        (provider = 'LOCAL' AND password_hash IS NOT NULL)
        OR (provider <> 'LOCAL' AND provider_id IS NOT NULL)
    );
