CREATE TABLE refresh_tokens (
    token_id   UUID         NOT NULL,
    token      VARCHAR(255) NOT NULL,
    user_id    UUID         NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT pk_refresh_tokens          PRIMARY KEY (token_id),
    CONSTRAINT uq_refresh_tokens_token    UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user     FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);
