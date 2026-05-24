# Database Guidelines

## Миграции Liquibase

Все изменения схемы базы данных выполняются только через Liquibase.

Ручные изменения в базе данных запрещены.

В проекте используется подход: **XML-файлы только подключают changelog-файлы, реальные изменения БД хранятся в formatted SQL-файлах**.

То есть:

- `master.xml` подключает месячные changelog-файлы;
- месячные `YYYY.MM-changelog.xml` подключают SQL-миграции за конкретный месяц;
- все реальные DDL/DML-изменения пишутся в `.sql` файлах;
- SQL-файлы обязательно используют синтаксис Liquibase formatted SQL.

## Структура Liquibase-файлов

Файлы Liquibase должны храниться в `src/main/resources/liquibase`.

```text
src/main/resources/liquibase/
├── master.xml
└── changelog/
    └── YYYY.MM/
        ├── YYYY.MM-changelog.xml
        └── YYYY.MM.DD_TASK-NUMBER.sql
```

Пример:

```text
src/main/resources/liquibase/
├── master.xml
└── changelog/
    └── 2026.05/
        ├── 2026.05-changelog.xml
        └── 2026.05.13_PPO-184.sql
```

Не следует хранить новые миграции в структуре вида:

```text
src/main/resources/db/changelog/2026/05/master.xml
```

Для новых изменений используется только структура:

```text
src/main/resources/liquibase/changelog/YYYY.MM/
```

## Настройка Spring Boot

В `application.yml` должен быть указан путь к основному changelog-файлу:

```yaml
spring:
  liquibase:
    change-log: classpath:liquibase/master.xml
```

## Главный changelog `master.xml`

Файл `src/main/resources/liquibase/master.xml` не должен содержать DDL/DML-изменений. Он используется только для подключения месячных changelog-файлов.

Пример:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="
            http://www.liquibase.org/xml/ns/dbchangelog
            https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <include file="liquibase/changelog/2026.05/2026.05-changelog.xml"
             relativeToChangelogFile="false"/>

</databaseChangeLog>
```

## Месячный changelog

Месячный changelog подключает SQL-миграции за конкретный месяц.

Пример файла `src/main/resources/liquibase/changelog/2026.05/2026.05-changelog.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="
            http://www.liquibase.org/xml/ns/dbchangelog
            https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <include file="liquibase/changelog/2026.05/2026.05.13_PPO-184.sql"
             relativeToChangelogFile="false"/>

</databaseChangeLog>
```

## SQL-миграции

Все реальные изменения базы данных должны храниться в `.sql` файлах.

Каждый SQL-файл миграции должен начинаться со строки:

```sql
--liquibase formatted sql
```

Каждый changeset должен содержать:

- заголовок changeset;
- комментарий;
- SQL-изменение;
- rollback.

Пример:

```sql
--liquibase formatted sql

--changeset ivanov:PPO-184-2026.05.13-001
--comment: создание таблицы справочников
CREATE TABLE dictionaries (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(500) NOT NULL,
    has_ext_id BOOLEAN NOT NULL DEFAULT FALSE,
    is_editable BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

--rollback DROP TABLE IF EXISTS dictionaries;
```

Для no-op/baseline миграции можно использовать:

```sql
--liquibase formatted sql

--changeset ivanov:PPO-000-2026.05.13-001
--comment: базовая миграция для reference-service
SELECT 1;

--rollback SELECT 1;
```

## Именование файлов миграций

Файлы SQL-миграций называются по шаблону:

```text
YYYY.MM.DD_TASK-NUMBER.sql
```

Примеры:

```text
2026.05.13_PPO-184.sql
2026.05.13_PPO-184-001.sql
2026.05.14_PPO-185.sql
```

Где:

- `YYYY.MM.DD` — дата создания миграции;
- `TASK-NUMBER` — номер задачи из Jira;
- дополнительный порядковый номер можно добавлять, если в рамках одной задачи несколько миграций.

## Правила для миграций

- Реальные изменения БД не пишутся в XML.
- XML используется только как индекс для подключения changelog-файлов.
- Каждый changeset должен иметь осмысленный `--comment`.
- Каждый changeset должен иметь `--rollback`.
- Миграции коммитятся вместе с кодом, который от них зависит.
- Нельзя изменять уже примененные changeset-файлы, если они могли быть выполнены в общей среде.
- Для исправления уже примененной миграции создается новый changeset.
- Не следует объединять несвязанные изменения в один changeset.
- Один changeset должен отвечать за одно логическое изменение.

## Запрет на `nativeQuery`

Использование `nativeQuery` запрещено, кроме случаев, когда задачу нельзя разумно реализовать через JPA, JPQL, Criteria API или Specification.

Причины запрета:

- ухудшение переносимости кода;
- сложность поддержки;
- потеря преимуществ ORM;
- высокая вероятность SQL-ошибок;
- сложнее тестировать и рефакторить запросы.

Если `nativeQuery` всё же требуется, необходимо:

- обосновать это в merge request;
- описать, почему JPA/JPQL/Specification не подходят;
- покрыть запрос тестами;
- проверить нетривиальный запрос через `EXPLAIN ANALYZE`.

## DTO Projections

DTO Projections в Spring Data JPA позволяют извлекать из базы только необходимые данные вместо полной загрузки Entity.

Использование projections рекомендуется для read-only сценариев, где не требуется изменять загруженную сущность.

Projections стоит использовать:

- для списков, где на frontend нужно отобразить только часть колонок;
- для легковесных справочников и выпадающих списков;
- для агрегатов и отчетов;
- для read-only API;
- для снижения объема данных между БД и приложением.

Entity не должны возвращаться наружу через REST API.

## Транзакции

`@Transactional` ставится только на методы сервисного слоя.

Для read-only операций используется:

```java
@Transactional(readOnly = true)
```

Правила:

- не ставить `@Transactional` на controller;
- не ставить `@Transactional` на repository без необходимости;
- не открывать транзакцию шире, чем требуется;
- не выполнять внешние HTTP-вызовы внутри транзакции без необходимости;
- операции создания, изменения и смены статуса должны выполняться в транзакции.

## Индексы и производительность

Индексы добавляются только при наличии понятного сценария использования.

Перед добавлением индекса нужно оценить:

- какой запрос будет использовать индекс;
- насколько селективно поле;
- насколько часто таблица изменяется;
- не покрывает ли существующий составной индекс этот сценарий;
- как индекс влияет на `INSERT`, `UPDATE`, `DELETE`.

Для нетривиальных запросов необходимо проверять план выполнения через:

```sql
EXPLAIN ANALYZE
```

Недостаточно просто увидеть `Index Scan`. Нужно убедиться, что планировщик действительно выбирает ожидаемый индекс и время выполнения соответствует ожиданиям.

## Что важно для Codex

При создании или изменении миграций Codex должен соблюдать следующие правила:

- не создавать DDL/DML changeset в XML;
- использовать только Liquibase formatted SQL для реальных изменений;
- начинать SQL-файлы со строки `--liquibase formatted sql`;
- добавлять `--changeset`, `--comment` и `--rollback`;
- подключать SQL-файлы через месячный `YYYY.MM-changelog.xml`;
- подключать месячный changelog через `liquibase/master.xml`;
- не создавать бизнес-таблицы без явного указания в задаче;
- не изменять уже примененные миграции, если они могли быть выполнены в общей среде.
