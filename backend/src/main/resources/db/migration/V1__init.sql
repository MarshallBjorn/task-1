CREATE TABLE IF NOT EXISTS "currency" (
    "code" VARCHAR(3) PRIMARY KEY,
    "name" VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS "trip_role" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS "users" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" VARCHAR(50) NOT NULL,
    "email" VARCHAR(255) UNIQUE NOT NULL,
    "password" VARCHAR(255) NOT NULL,
    "role" VARCHAR(50) NOT NULL,
    "photo_url" VARCHAR(500),
    "currency_code" VARCHAR(3),
    "is_verified" BOOLEAN NOT NULL DEFAULT false,
    "is_blocked" BOOLEAN NOT NULL DEFAULT false,
    
    CONSTRAINT "fk_user_currency" FOREIGN KEY ("currency_code") REFERENCES "currency"("code")
);

CREATE TABLE IF NOT EXISTS "trip_event" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "owner_id" UUID NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "currency_code" VARCHAR(3) NOT NULL,
    "start_date" DATE NOT NULL,
    "end_date" DATE NOT NULL,
    "budget" DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    
    CONSTRAINT "fk_trip_owner" FOREIGN KEY ("owner_id") REFERENCES "users"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_trip_currency" FOREIGN KEY ("currency_code") REFERENCES "currency"("code")
);

CREATE TABLE IF NOT EXISTS "trip_participant" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "event_id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "trip_role_id" UUID NOT NULL,
    "personal_budget" DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    "is_accepted" BOOLEAN NOT NULL DEFAULT false,
    
    CONSTRAINT "fk_participant_event" FOREIGN KEY ("event_id") REFERENCES "trip_event"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_participant_user" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_participant_role" FOREIGN KEY ("trip_role_id") REFERENCES "trip_role"("id"),
    CONSTRAINT "uq_event_user" UNIQUE ("event_id", "user_id") 
);