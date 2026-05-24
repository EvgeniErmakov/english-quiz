--liquibase formatted sql

--changeset codex:REVIEW-001-2026.05.24
--comment: create card_states table
CREATE TABLE card_states (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users (id),
    card_id UUID NOT NULL REFERENCES cards (id),
    difficulty NUMERIC(6, 3) NOT NULL DEFAULT 0,
    stability NUMERIC(6, 3) NOT NULL DEFAULT 0,
    retrievability NUMERIC(6, 3),
    repetitions INTEGER NOT NULL DEFAULT 0,
    lapses INTEGER NOT NULL DEFAULT 0,
    last_reviewed_at TIMESTAMPTZ,
    next_review_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ux_card_states_user_card UNIQUE (user_id, card_id)
);

CREATE INDEX idx_card_states_user_next_review ON card_states (user_id, next_review_at);
CREATE INDEX idx_card_states_card_id ON card_states (card_id);

--rollback DROP INDEX IF EXISTS idx_card_states_card_id;
--rollback DROP INDEX IF EXISTS idx_card_states_user_next_review;
--rollback DROP TABLE IF EXISTS card_states;

--changeset codex:REVIEW-002-2026.05.24
--comment: create review_attempts table
CREATE TABLE review_attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users (id),
    card_id UUID NOT NULL REFERENCES cards (id),
    card_state_id UUID NOT NULL REFERENCES card_states (id),
    user_answer VARCHAR(1000) NOT NULL,
    expected_answer VARCHAR(500) NOT NULL,
    normalized_user_answer VARCHAR(1000) NOT NULL,
    normalized_expected_answer VARCHAR(500) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    rating VARCHAR(50) NOT NULL,
    ai_feedback VARCHAR(2000),
    response_time_ms INTEGER,
    reviewed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_review_attempts_user_reviewed ON review_attempts (user_id, reviewed_at);
CREATE INDEX idx_review_attempts_card_reviewed ON review_attempts (card_id, reviewed_at);
CREATE INDEX idx_review_attempts_card_state ON review_attempts (card_state_id);

--rollback DROP INDEX IF EXISTS idx_review_attempts_card_state;
--rollback DROP INDEX IF EXISTS idx_review_attempts_card_reviewed;
--rollback DROP INDEX IF EXISTS idx_review_attempts_user_reviewed;
--rollback DROP TABLE IF EXISTS review_attempts;
