  CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR,
    completed BOOLEAN DEFAULT FALSE,
    added_at TIMESTAMP
  );