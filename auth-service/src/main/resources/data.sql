-- Ensure the 'users' table exists

CREATE TABLE IF NOT EXISTS "users" (
    id UUID PRIMARY KEY,  -- Correct primary key
    user_id UUID UNIQUE NOT NULL,                   -- Business-level ID, not PK
    email VARCHAR(255) UNIQUE NOT NULL,
    encoded_password VARCHAR(255),
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

-- Insert the user if no existing user with the same id or email exists

INSERT INTO "users" (id, user_id, email, encoded_password, provider, provider_user_id, created_at)
SELECT '99999999-8888-7777-6666-555555555555', -- auto-generate DB PK
       '11111111-2222-3333-4444-555555555555',
       'dummy@example.com',
       '$2a$12$PFBUjyADDfV3QQui42SL6OrJgzv.GwHKjYh.n./GyqGadNB6/Gjrq',
       'LOCAL',
       NULL,
       now()
WHERE NOT EXISTS (
    SELECT 1
    FROM "users"
    WHERE user_id = '11111111-2222-3333-4444-555555555555'
       OR email = 'dummy@example.com'
);

-- Ensure the 'refresh_tokens' table exists
CREATE TABLE IF NOT EXISTS "refresh_tokens" (
    id UUID PRIMARY KEY,
    token TEXT UNIQUE NOT NULL,
    user_id UUID NOT NULL REFERENCES "users"(id),
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

-- Insert the refresh token if no existing refresh token with the same id or token exists
INSERT INTO "refresh_tokens" (id, token, user_id, issued_at, expires_at)
SELECT 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
       'sample-refresh-token-123456',
       '99999999-8888-7777-6666-555555555555',
       now(),
       now() + INTERVAL '30 days'
WHERE NOT EXISTS (
    SELECT 1
    FROM "refresh_tokens"
    WHERE id = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'
       OR token = 'sample-refresh-token-123456'
);
