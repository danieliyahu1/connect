CREATE TABLE IF NOT EXISTS "trips" (
    id UUID PRIMARY KEY,
    public_id UUID UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    country VARCHAR(50) NOT NULL,
    city VARCHAR(50),
    start_date DATE,
    end_date DATE
);

INSERT INTO "trips" (id, public_id, user_id, country, city, start_date, end_date)
SELECT 'ccccccc3-dddd-eeee-ffff-000000000000',
       '11111111-1111-1111-1111-111111111111',
       '11111111-2222-3333-4444-555555555555',
       'GERMANY',
       'BERLIN',
       current_date + INTERVAL '10 days',
       current_date + INTERVAL '17 days'
WHERE NOT EXISTS (
    SELECT 1 FROM "trips"
    WHERE id = 'ccccccc3-dddd-eeee-ffff-000000000000'
);