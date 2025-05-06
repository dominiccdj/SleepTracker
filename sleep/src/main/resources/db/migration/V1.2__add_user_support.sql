-- V1.2__add_user_support.sql
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE
);

-- Add user_id column to sleep_logs table
ALTER TABLE sleep_logs ADD COLUMN user_id BIGINT;
ALTER TABLE sleep_logs ADD CONSTRAINT fk_sleep_logs_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Add comments for better documentation
COMMENT ON TABLE users IS 'Stores user information';
COMMENT ON COLUMN users.id IS 'Unique identifier for each user';
COMMENT ON COLUMN users.username IS 'Unique username for the user';
COMMENT ON COLUMN users.email IS 'Unique email address for the user';
COMMENT ON COLUMN sleep_logs.user_id IS 'Reference to the user who created this sleep log';
