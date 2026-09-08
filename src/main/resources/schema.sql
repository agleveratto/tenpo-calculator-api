CREATE TABLE IF NOT EXISTS api_call_logs (
                                             id SERIAL PRIMARY KEY,
                                             timestamp TIMESTAMP NOT NULL,
                                             endpoint VARCHAR(255) NOT NULL,
    parameters TEXT,
    response TEXT,
    error TEXT
    );