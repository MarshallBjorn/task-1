-- 1. Harmonogram / Punkty wyjazdu (TRIP_NODE)
CREATE TABLE IF NOT EXISTS trip_node (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    reporter_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL,
    note TEXT,
    price DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    is_separate BOOLEAN NOT NULL DEFAULT false,
    
    CONSTRAINT fk_node_event FOREIGN KEY (event_id) REFERENCES trip_event(id) ON DELETE CASCADE,
    CONSTRAINT fk_node_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Posty / Wspomnienia (TRIP_POST)
CREATE TABLE IF NOT EXISTS trip_post (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    node_id UUID NOT NULL,
    reporter_id UUID NOT NULL,
    note TEXT NOT NULL,
    
    CONSTRAINT fk_post_node FOREIGN KEY (node_id) REFERENCES trip_node(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Zdjęcia do postów (TRIP_PHOTO)
CREATE TABLE IF NOT EXISTS trip_photo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    
    CONSTRAINT fk_photo_post FOREIGN KEY (post_id) REFERENCES trip_post(id) ON DELETE CASCADE
);