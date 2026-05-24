# Codex Workflow

## Общий принцип

Codex не должен получать одну большую задачу на весь сервис. Лучше давать ему маленькие задачи с понятным результатом и проверками.

Правильная последовательность:

1. Инициализировать репозиторий и базовый Spring Boot проект.
2. Настроить Gradle, Java, зависимости и профили конфигурации.
3. Подключить quality tools: Checkstyle, тестовую инфраструктуру.
4. Подготовить Liquibase-каркас.
5. Создать модель данных и миграции.
6. Реализовать базовые read endpoints.
7. Реализовать create/update/status endpoints.
8. Добавить версионирование, фильтрацию, поиск, count.
9. Покрыть функциональность тестами.

## Что нельзя просить на первом шаге

Не проси Codex сразу:

- реализовать все справочники;
- создать все endpoint'ы;
- писать бизнес-логику версионирования;
- делать полный security flow;
- одновременно делать OpenAPI, Entity, Controller, Service, Repository, Liquibase и тесты.

Такой prompt почти всегда приводит к смешению слоев и некачественному коду.

## Как формулировать задачи

Хороший prompt должен содержать:

- цель задачи;
- ссылки на `AGENTS.md` и нужный документ из `docs/`;
- конкретные файлы/слои, которые можно менять;
- что не нужно делать;
- команды проверки;
- ожидаемый результат.

## Шаблон prompt'а

```text
Read AGENTS.md and the relevant files from docs/ before making changes.

Task:
<short task description>

Requirements:
- <requirement 1>
- <requirement 2>

Do not:
- <explicit restriction 1>
- <explicit restriction 2>

After changes, run:
- ./gradlew clean build
- ./gradlew test, if tests were added or changed
- ./gradlew check, if Checkstyle is configured

In the final response, explain:
- what was changed;
- which files were changed;
- how the result was verified.
```

## Review checklist

Перед принятием результата проверь:

- нет ли бизнес-логики в controller;
- не возвращаются ли Entity через API;
- есть ли DTO для request/response;
- есть ли rollback в Liquibase;
- нет ли секретов в конфигурации;
- есть ли тесты для новой логики;
- проходит ли сборка;
- понятны ли имена классов, методов и пакетов.
