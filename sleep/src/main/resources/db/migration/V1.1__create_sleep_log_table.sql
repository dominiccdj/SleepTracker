-- V1.1__create_sleep_log_table.sql
CREATE TABLE sleep_logs (
                            id SERIAL PRIMARY KEY,
                            date DATE NOT NULL,
                            bed_time TIMESTAMP NOT NULL,
                            wake_time TIMESTAMP NOT NULL,
                            time_in_bed_minutes INTEGER NOT NULL,
                            morning_feeling VARCHAR(10) NOT NULL CHECK (morning_feeling IN ('BAD', 'OK', 'GOOD'))
);

-- Add index on date for faster queries
CREATE INDEX idx_sleep_logs_date ON sleep_logs(date);

-- Add comments for better documentation
COMMENT ON TABLE sleep_logs IS 'Stores user sleep data including time in bed and morning feeling';
COMMENT ON COLUMN sleep_logs.id IS 'Unique identifier for each sleep log entry';
COMMENT ON COLUMN sleep_logs.date IS 'The date of the sleep (typically today)';
COMMENT ON COLUMN sleep_logs.bed_time IS 'Timestamp when the user went to bed';
COMMENT ON COLUMN sleep_logs.wake_time IS 'Timestamp when the user woke up';
COMMENT ON COLUMN sleep_logs.time_in_bed_minutes IS 'Total minutes spent in bed (calculated from bed_time and wake_time)';
COMMENT ON COLUMN sleep_logs.morning_feeling IS 'How the user felt in the morning: BAD, OK, or GOOD';
