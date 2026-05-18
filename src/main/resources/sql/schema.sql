-- Session table
CREATE TABLE IF NOT EXISTS session (
    id VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    ip VARCHAR(255),
    status BOOLEAN DEFAULT true,
    query TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT session_pkey PRIMARY KEY (id)
);

-- Session chat state table
CREATE TABLE IF NOT EXISTS session_chat (
    session_id VARCHAR(255) NOT NULL,
    state_key VARCHAR(255) NOT NULL,
    item_index INTEGER NOT NULL,
    state_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT session_chat_pkey PRIMARY KEY (session_id, state_key, item_index)
);

CREATE INDEX IF NOT EXISTS idx_session_chat_session_id ON session_chat(session_id);
