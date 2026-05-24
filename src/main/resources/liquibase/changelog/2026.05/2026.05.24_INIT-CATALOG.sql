--liquibase formatted sql

--changeset codex:CATALOG-001-2026.05.24
--comment: create app_users table
CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    telegram_user_id BIGINT,
    username VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX ux_app_users_telegram_user_id
    ON app_users (telegram_user_id)
    WHERE telegram_user_id IS NOT NULL;

--rollback DROP INDEX IF EXISTS ux_app_users_telegram_user_id;
--rollback DROP TABLE IF EXISTS app_users;

--changeset codex:CATALOG-002-2026.05.24
--comment: create decks table
CREATE TABLE decks (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_users (id),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_decks_owner_id ON decks (owner_id);

--rollback DROP INDEX IF EXISTS idx_decks_owner_id;
--rollback DROP TABLE IF EXISTS decks;

--changeset codex:CATALOG-003-2026.05.24
--comment: create cards table
CREATE TABLE cards (
    id UUID PRIMARY KEY,
    deck_id UUID NOT NULL REFERENCES decks (id),
    russian_prompt VARCHAR(500) NOT NULL,
    english_answer VARCHAR(500) NOT NULL,
    example_sentence VARCHAR(1000),
    example_translation VARCHAR(1000),
    part_of_speech VARCHAR(50),
    level VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ux_cards_deck_ru_en UNIQUE (deck_id, russian_prompt, english_answer)
);

CREATE INDEX idx_cards_deck_id ON cards (deck_id);

--rollback DROP INDEX IF EXISTS idx_cards_deck_id;
--rollback DROP TABLE IF EXISTS cards;

--changeset codex:CATALOG-004-2026.05.24
--comment: create card_answer_aliases table
CREATE TABLE card_answer_aliases (
    id UUID PRIMARY KEY,
    card_id UUID NOT NULL REFERENCES cards (id),
    alias VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ux_card_answer_aliases_card_alias UNIQUE (card_id, alias)
);

CREATE INDEX idx_card_answer_aliases_card_id ON card_answer_aliases (card_id);

--rollback DROP INDEX IF EXISTS idx_card_answer_aliases_card_id;
--rollback DROP TABLE IF EXISTS card_answer_aliases;
