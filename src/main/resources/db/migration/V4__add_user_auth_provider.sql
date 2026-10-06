-- Federated sign-in needs a user row with no password, and a stable link to the provider's own id.
ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE users
    ADD COLUMN provider    VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN provider_id VARCHAR(255);

-- One account per provider identity. LOCAL rows keep provider_id NULL, which Postgres treats as
-- distinct, so this does not constrain password accounts.
ALTER TABLE users
    ADD CONSTRAINT uq_users_provider_identity UNIQUE (provider, provider_id);

-- A row must be able to authenticate somehow: a password, or a provider identity.
ALTER TABLE users
    ADD CONSTRAINT ck_users_credentials CHECK (
        (provider = 'LOCAL' AND password_hash IS NOT NULL)
        OR (provider <> 'LOCAL' AND provider_id IS NOT NULL)
    );
