CREATE TABLE IF NOT EXISTS connectors (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    first_name VARCHAR(100),
    country VARCHAR(100),
    city VARCHAR(100),
    bio TEXT
);

CREATE TABLE IF NOT EXISTS connector_images (
    id UUID PRIMARY KEY,
    connector_id UUID NOT NULL REFERENCES connectors(id) ON DELETE CASCADE,
    media_id TEXT NOT NULL UNIQUE,
    order_index INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS social_media (
    id UUID PRIMARY KEY,
    connector_id UUID NOT NULL REFERENCES connectors(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    profile_url TEXT NOT NULL,
    UNIQUE (connector_id, platform)
);

INSERT INTO connectors (id, user_id, first_name, country, city, bio)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
       '11111111-2222-3333-4444-555555555555',
       'Daniel',
       'POLAND',
       'KRAKOW',
       'I love meeting new people in hostels!'
WHERE NOT EXISTS (
    SELECT 1 FROM connectors WHERE user_id = '11111111-2222-3333-4444-555555555555'
);

INSERT INTO connector_images (id, connector_id, media_id, order_index)
SELECT 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
       'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
       'https://cdn.test.com/image.jpg',
       0
WHERE NOT EXISTS (
    SELECT 1 FROM connector_images WHERE media_id = 'https://cdn.test.com/image.jpg'
);

INSERT INTO social_media (id, connector_id, platform, profile_url)
SELECT 'cccccccc-cccc-cccc-cccc-cccccccccccc',
       'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
       'INSTAGRAM',
       'https://instagram.com/testuser'
WHERE NOT EXISTS (
    SELECT 1 FROM social_media 
    WHERE connector_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
      AND platform = 'INSTAGRAM'
);