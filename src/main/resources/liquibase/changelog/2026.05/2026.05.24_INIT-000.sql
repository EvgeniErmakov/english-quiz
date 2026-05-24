--liquibase formatted sql

--changeset codex:INIT-000-2026.05.24-001
--comment: baseline no-op migration
SELECT 1;

--rollback SELECT 1;
