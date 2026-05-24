# Эндпоинты типовых справочников

Документ описывает проектный контракт REST API для типовых справочников Reference service. Он предназначен как основа для последующего описания OpenAPI.

## Общие принципы

- Все типовые справочники доступны через единый API по `dictionaryCode`.
- `dictionaryCode` соответствует машинному коду справочника из `ref_dictionaries`, например `outageKinds`, `resourceTypes`, `changeReasons`, `changeTypes.`
- Каждый справочник физически хранится в собственной таблице, но наружу отдается через единый контракт.
- Метаинформация справочника возвращается отдельным endpoint `GET /api/v1/reference/dictionaries/{dictionaryCode}/metadata`.
- Эндпоинты возвращают только данные сценария: записи, страницу, счетчики или результат изменения. Они не дублируют metadata.
- Metadata справочника должна покрывать таблицу реестра, карточку просмотра, форму создания, форму редактирования, панель фильтров и список версий.
- Изменение записи создает новую версию записи (поле version).
- История версий хранится в строках конкретной справочной таблицы - <https://wiki.lanit.ru/pages/viewpage.action?pageId=718964172&clckid=20c01170#:~:text=%D0%96%D0%B8%D0%B7%D0%BD%D0%B5%D0%BD%D0%BD%D1%8B%D0%B9%20%D1%86%D0%B8%D0%BA%D0%BB%20%D0%B2%D0%B5%D1%80%D1%81%D0%B8%D0%B8>
- Для связи версий одной логической записи используется `itemId`; актуальная версия определяется по `isCurrent = true`.
- Операции изменения доступны только для справочников, у которых `isEditable = true`.
- Для экранов реестра счетчики вкладок `Все`, `Активные`, `Неактивные` возвращаются отдельным endpoint `count`.

## Структура хранения

### `ref_dictionaries`

Реестр справочников содержит настройки доступности и отображения справочников. Физические таблицы справочников остаются в snake\_case, например `ref_outage_kinds`, а публичный `dictionaryCode` в API используется в camelCase, например `outageKinds`.

| Поле БД | Тип БД | Поле API | Описание |
| --- | --- | --- | --- |
| `id` | uuid PK | `id` | Уникальный идентификатор справочника. |
| `code` | varchar(50) | `code` | Машинный код справочника для API, например `outageKinds`. |
| `name` | varchar(500) | `name` | Отображаемое название справочника для UI. |
| `has_ext_id` | boolean | `hasExtId` | Есть ли числовой ID для интеграции. |
| `is_editable` | boolean | `isEditable` | Можно ли изменять справочник через UI. |
| `is_active` | boolean | `isActive` | Активен ли справочник. |
| `created_at` | timestamptz | `createdAt` | Дата создания. |
| `updated_at` | timestamptz | `updatedAt` | Дата обновления. |

### Справочные таблицы

Для типовых справочников используются отдельные таблицы: `ref_outage_kinds`, `ref_resource_types`, `ref_change_reasons`, `ref_change_types`. Базовый набор колонок одинаковый.

| Поле БД | Тип БД | Поле API | Описание |
| --- | --- | --- | --- |
| `id` | uuid PK | `id` | Уникальный идентификатор версии записи. |
| `item_id` | uuid | `itemId` | Уникальный идентификатор логической записи. Один `itemId` объединяет все версии записи. |
| `ext_id` | integer | `extId` | Числовой ID для интеграции, если справочник его поддерживает. |
| `code` | varchar(50) | `code` | Машиночитаемый код значения справочника. |
| `name` | varchar(500) | `name` | Отображаемое название значения справочника. |
| `is_active` | boolean | `isActive` | Бизнес-активность записи. |
| `version` | integer | `version` | Номер версии логической записи. |
| `is_current` | boolean | `isCurrent` | Признак актуальной версии. Для одного `itemId` только одна строка может иметь `isCurrent = true`. |
| `created_at` | timestamptz | `createdAt` | Дата создания версии. |
| `updated_at` | timestamptz | `updatedAt` | Дата технического обновления строки. |

Списки и счетчики справочника работают только с актуальными версиями (`isCurrent = true`). Архивные версии возвращаются только через endpoint списка версий записи.

Фронтенд может отображать даты в виде `dd.MM.yyyy HH:mm`, но API передает и принимает даты как `Instant`, например `2026-05-07T10:00:00.000Z`.

## Общие модели

### DictionarySummary

```json
{
  "id": "8e05c94a-4fd2-40e8-9e62-8d2c1ad38f94",
  "code": "outageKinds",
  "name": "Виды отключений",
  "hasExtId": true,
  "isEditable": true,
  "isActive": true,
  "createdAt": "2026-05-07T10:00:00.000Z",
  "updatedAt": "2026-05-07T10:00:00.000Z"
}
```

### FieldMetadata

```json
{
  "key": "name",
  "title": "Наименование",
  "type": "string",
  "required": true
}
```

Поля `FieldMetadata` нужны как базовый каталог полей справочника:

- `key` - стабильное имя поля в JSON-объекте записи. Используется для чтения значения из `item`, отправки `sort`, связи фильтра с query-параметром и обработки формы.
- `title` - человекочитаемый заголовок поля для таблицы, карточки и формы.
- `type` - тип значения для выбора UI-контрола, форматирования и базовой клиентской валидации. Рекомендуемые значения: `uuid`, `integer`, `string`, `boolean`, `datetime`, `object`.
- `required` - признак обязательности поля в форме создания или изменения и подсказка для валидации DTO.

### DictionaryItem

```json
{
  "id": "b9fe8f18-a8e6-43a2-9dd5-2c134c879e50",
  "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
  "extId": 1,
  "code": "planned",
  "name": "Плановое",
  "isActive": true,
  "version": 1,
  "isCurrent": true,
  "createdAt": "2026-05-07T10:00:00.000Z",
  "updatedAt": "2026-05-07T10:00:00.000Z"
}
```

`id` - идентификатор конкретной строки-версии. `itemId` - стабильный идентификатор логической записи и используется в маршрутах `items/{itemId}`.

### PageMetadata

```json
{
  "page": 0,
  "size": 10,
  "totalElements": 135,
  "totalPages": 14
}
```

### ErrorResponse

```json
{
  "code": "DICTIONARY_ITEM_DUPLICATE",
  "message": "Запись с таким кодом уже существует",
  "details": {
    "field": "code",
    "value": "planned"
  },
  "timestamp": "2026-05-07T10:00:00.000Z"
}
```

## **1. Получение списка справочников**

Возвращает справочники, зарегистрированные в `ref_dictionaries`.

```http
GET /api/v1/reference/dictionaries
```

### Query-параметры

| Параметр | Тип | Обязательный | Описание |
| --- | --- | --- | --- |
| `activeOnly` | boolean | нет | Если `true`, вернуть только активные справочники. По умолчанию `true`. |
| `editableOnly` | boolean | нет | Если `true`, вернуть только редактируемые справочники. |

### Ответ 200

```json
{
  "items": [
    {
      "id": "8e05c94a-4fd2-40e8-9e62-8d2c1ad38f94",
      "code": "outageKinds",
      "name": "Виды отключений",
      "hasExtId": true,
      "isEditable": true,
      "isActive": true,
      "createdAt": "2026-05-07T10:00:00.000Z",
      "updatedAt": "2026-05-07T10:00:00.000Z"
    }
  ]
}
```

## **2. Получение метаинформации справочника**

Возвращает метаинформацию, необходимую frontend для построения таблицы реестра, карточки просмотра, формы создания, формы редактирования, панели фильтров и списка версий.

```http
GET /api/v1/reference/dictionaries/{dictionaryCode}/metadata
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника из `ref_dictionaries.code`. |

### Структура ответа

Metadata разделяется по сценариям, а не возвращается одним общим массивом `columns`.

```json
{
  "dictionary": {},
  "fields": {},
  "table": {},
  "view": {},
  "createForm": {},
  "editForm": {},
  "filters": [],
  "sorts": [],
  "versions": {}
}
```

- `dictionary` - краткое описание справочника.
- `fields` - единый каталог полей справочника по ключу поля.
- `table` - настройки таблицы реестра и список колонок таблицы. Отвечает на вопрос: какие столбцы необходимо отображать на странице реестра.
- `view` - поля карточки просмотра.
- `createForm` - поля формы создания.
- `editForm` - поля формы редактирования.
- `filters` - фильтры, которые frontend показывает в панели фильтров и отправляет как query-параметры.
- `sorts` - поля, которые разрешено передавать в query-параметре `sort` .
- `versions` - настройки списка версий записи.

Во всех сценарных блоках metadata ссылка на поле записи задается через `key`. Значение `key` всегда означает имя поля в JSON-объекте записи и должно совпадать с одним из ключей объекта `fields`.

Поэтому сценарные списки `table.columns`, `view.fields` и `versions.columns` возвращаются массивами объектов с `key`, а не массивами строк.

### Пример: ответ 200 для `outageKinds`

```json
{
  "dictionary": {
    "code": "outageKinds",
    "name": "Виды отключений",
    "hasExtId": true,
    "isEditable": true
  },
  "fields": {
    "id": {
      "key": "id",
      "title": "Идентификатор версии",
      "type": "uuid",
      "required": true
    },
    "itemId": {
      "key": "itemId",
      "title": "Идентификатор записи",
      "type": "uuid",
      "required": true
    },
    "extId": {
      "key": "extId",
      "title": "Код",
      "type": "integer",
      "required": true
    },
    "name": {
      "key": "name",
      "title": "Вид отключения",
      "type": "string",
      "required": true
    },
    "code": {
      "key": "code",
      "title": "Название машиночитаемое",
      "type": "string",
      "required": true
    },
    "isActive": {
      "key": "isActive",
      "title": "Статус",
      "type": "boolean",
      "required": true
    },
    "isCurrent": {
      "key": "isCurrent",
      "title": "Актуальная версия",
      "type": "boolean",
      "required": true
    },
    "createdAt": {
      "key": "createdAt",
      "title": "Дата создания",
      "type": "datetime",
      "required": true
    },
    "updatedAt": {
      "key": "updatedAt",
      "title": "Дата изменения",
      "type": "datetime",
      "required": false
    },
    "version": {
      "key": "version",
      "title": "Версия",
      "type": "integer",
      "required": true
    }
  },
  "table": {
    "columns": [
      {
        "key": "extId"
      },
      {
        "key": "name"
      },
      {
        "key": "code"
      },
      {
        "key": "createdAt"
      }
    ]
  },
  "view": {
    "fields": [
      {
        "key": "extId"
      },
      {
        "key": "name"
      },
      {
        "key": "code"
      },
      {
        "key": "isActive"
      },
      {
        "key": "createdAt"
      },
      {
        "key": "updatedAt"
      },
      {
        "key": "version"
      }
    ]
  },
  "createForm": { - возможно здесь нужно передавать ограничения по длине max, min, паттерны и regex. Уточнить
    "fields": [
      {
        "key": "extId",
        "editable": true,
        "required": true
      },
      {
        "key": "name",
        "editable": true,
        "required": true
      },
      {
        "key": "code",
        "editable": true,
        "required": true
      }
    ]
  },
  "editForm": {
    "concurrencyKey": "version",
    "fields": [
      {
        "key": "extId",
        "editable": false,
        "required": true
      },
      {
        "key": "name",
        "editable": true,
        "required": true
      },
      {
        "key": "code",
        "editable": true,
        "required": true
      },
      {
        "key": "createdAt",
        "editable": false,
        "required": true
      },
      {
        "key": "updatedAt",
        "editable": false,
        "required": false
      },
      {
        "key": "version",
        "editable": false,
        "required": true
      }
    ]
  },
  "filters": [
    {
      "key": "name",
      "queryParam": "name",
      "filterable": true,
      "filterType": "text"
    },
    {
      "key": "code",
      "queryParam": "code",
      "filterable": true,
      "filterType": "text"
    },
    {
      "key": "createdAt",
      "queryParamFrom": "createdAtFrom",
      "queryParamTo": "createdAtTo",
      "filterable": true,
      "filterType": "datetimeRange"
    },
    {
      "key": "updatedAt",
      "queryParamFrom": "updatedAtFrom",
      "queryParamTo": "updatedAtTo",
      "filterable": true,
      "filterType": "datetimeRange"
    }
  ],
  "sorts": [ - если общая, тогда удалить из метоинформации и указать на фронте
    {
      "key": "extId",
      "sortable": true,
      "sort": "extId"
    },
    {
      "key": "name",
      "sortable": true,
      "sort": "name"
    },
    {
      "key": "code",
      "sortable": true,
      "sort": "code"
    },
    {
      "key": "createdAt",
      "sortable": true,
      "sort": "createdAt"
    },
    {
      "key": "updatedAt",
      "sortable": true,
      "sort": "updatedAt"
    },
    {
      "key": "version",
      "sortable": true,
      "sort": "version"
    }
  ],
  "versions": { - если общая, тогда удалить из метоинформации и указать на фронте ,
    "columns": [
      {
        "key": "version"
      },
      {
        "key": "createdAt"
      },
      {
        "key": "isCurrent"
      }
    ]
  }
}
```

### Ошибки

- `404 DICTIONARY_NOT_FOUND` - справочник с указанным кодом не найден.

## **3. Просмотр типового справочника**

Возвращает список актуальных записей на выбранной странице и данные пагинации. В список попадают только строки с `isCurrent = true`. Metadata для построения UI возвращается отдельным endpoint `GET /api/v1/reference/dictionaries/{dictionaryCode}/metadata`.

```http
GET /api/v1/reference/dictionaries/{dictionaryCode}
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника из `ref_dictionaries.code`. |

### Query-параметры

| Параметр | Тип | Обязательный | Описание |
| --- | --- | --- | --- |
| `page` | integer | нет | Номер страницы, начиная с `0`. По умолчанию `0`. |
| `size` | integer | нет | Количество записей на странице. По умолчанию `10`. Для UI ожидаемые значения: `10`, `20`, `30`. |
| `active` | boolean | нет | Фильтр по статусу записи. Если не передан, вернуть все записи. |
| `query` | string | нет | Общий поиск по `code`, `name`, при наличии также по `extId`. Минимальная длина непустого значения - 3 символа. |
| `name` | string | нет | Фильтр по пользовательскому названию значения, например по полю `Вид отключения`. Поиск без учета регистра, по вхождению. |
| `code` | string | нет | Фильтр по машиночитаемому названию. Поиск без учета регистра, по вхождению. |
| `createdAtFrom` | Instant | нет | Начало диапазона даты и времени создания включительно. ISO 8601 UTC, например `2026-02-06T21:00:00.000Z`. |
| `createdAtTo` | Instant | нет | Конец диапазона даты и времени создания включительно. ISO 8601 UTC, например `2026-05-31T20:59:59.999Z`. |
| `updatedAtFrom` | Instant | нет | Начало диапазона даты и времени изменения включительно. ISO 8601 UTC, например `2026-02-06T21:00:00.000Z`. |
| `updatedAtTo` | Instant | нет | Конец диапазона даты и времени изменения включительно. ISO 8601 UTC, например `2026-05-31T20:59:59.999Z`. |
| `sort` | string | нет | Spring Data сортировка в формате `{fieldKey},asc` или `{fieldKey},desc`, например `name,asc`, `code,desc`, `createdAt,desc`. Параметр можно передать несколько раз. |

Фильтры `query`, `name`, `code`, `createdAtFrom`, `createdAtTo`, `updatedAtFrom` и `updatedAtTo` должны совпадать по поведению с фильтрами метода `GET /api/v1/reference/dictionaries/{dictionaryCode}/count`, чтобы фронтенд мог получить страницу данных и количество записей по одним и тем же условиям.

Endpoint списка не возвращает счетчики вкладок. `page.totalElements` остается стандартным полем пагинации Spring Page для текущего запроса, включая выбранный `active`. Для вкладок фронтенд вызывает `GET /api/v1/reference/dictionaries/{dictionaryCode}/count` с теми же фильтрами поиска, но без `active`, `page`, `size` и `sort`.

### Примеры запросов

Тело запроса для `GET` не передается. Все параметры отправляются в URL после `?` в виде query-string. Несколько параметров разделяются символом `&`.

Получить первую страницу справочника с параметрами по умолчанию:

```http
GET /api/v1/reference/dictionaries/outageKinds
```

Получить вторую страницу по 20 записей:

```http
GET /api/v1/reference/dictionaries/outageKinds?page=1&size=20
```

Получить только активные записи и отсортировать по наименованию по возрастанию:

```http
GET /api/v1/reference/dictionaries/outageKinds?active=true&sort=name,asc
```

Получить активные записи с фильтрами из панели фильтров:

```http
GET /api/v1/reference/dictionaries/outageKinds?active=true&name=пла&code=planned&createdAtFrom=2026-02-06T21:00:00.000Z&createdAtTo=2026-05-31T20:59:59.999Z&updatedAtFrom=2026-02-06T21:00:00.000Z&updatedAtTo=2026-05-31T20:59:59.999Z
```

Получить только неактивные записи, найти по строке поиска и отсортировать по коду по убыванию:

```http
GET /api/v1/reference/dictionaries/outageKinds?active=false&query=planned&sort=code,desc
```

Пример с поисковой строкой на русском языке. Значение `query` должно быть URL-encoded:

```http
GET /api/v1/reference/dictionaries/outageKinds?query=пла&page=0&size=10
```

### Ответ 200

```json
{
  "items": [
    {
      "id": "b9fe8f18-a8e6-43a2-9dd5-2c134c879e50",
      "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
      "extId": 1,
      "code": "planned",
      "name": "Плановое",
      "isActive": true,
      "version": 1,
      "isCurrent": true,
      "createdAt": "2026-05-07T10:00:00.000Z",
      "updatedAt": "2026-05-07T10:00:00.000Z"
    }
  ],
  "page": {
    "page": 0,
    "size": 10,
    "totalElements": 369,
    "totalPages": 37
  }
}
```

### Ошибки

- `400 VALIDATION_ERROR` - некорректные фильтры, диапазоны дат или поисковая строка короче 3 символов.
- `404 DICTIONARY_NOT_FOUND` - справочник с указанным кодом не найден.

## **4. Получение количества записей**

Используется, чтобы получить количество актуальных записей справочника с учетом выбранных фильтров и разбивкой по статусам для вкладок реестра. Считаются только строки с `isCurrent = true`.

```http
GET /api/v1/reference/dictionaries/{dictionaryCode}/count
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника из `ref_dictionaries.code`. |

### Query-параметры

| Параметр | Тип | Обязательный | Описание |
| --- | --- | --- | --- |
| `query` | string | нет | Общий поиск по `code`, `name`, при наличии также по `extId`. Минимальная длина непустого значения - 3 символа. |
| `name` | string | нет | Фильтр по пользовательскому названию значения. |
| `code` | string | нет | Фильтр по машиночитаемому названию. |
| `createdAtFrom` | Instant | нет | Начало диапазона даты и времени создания включительно. ISO 8601 UTC, например `2026-02-06T21:00:00.000Z`. |
| `createdAtTo` | Instant | нет | Конец диапазона даты и времени создания включительно. ISO 8601 UTC, например `2026-05-31T20:59:59.999Z`. |
| `updatedAtFrom` | Instant | нет | Начало диапазона даты и времени изменения включительно. ISO 8601 UTC, например `2026-02-06T21:00:00.000Z`. |
| `updatedAtTo` | Instant | нет | Конец диапазона даты и времени изменения включительно. ISO 8601 UTC, например `2026-05-31T20:59:59.999Z`. |

Набор фильтров должен совпадать с фильтрами метода `GET /api/v1/reference/dictionaries/{dictionaryCode}`, кроме `active`, `page`, `size` и `sort`. Если позднее появятся дополнительные фильтры для списка записей, их нужно добавить и в этот метод.

`active` не передается, потому что метод сам возвращает общее количество и разбивку по статусам. `page`, `size` и `sort` не передаются, потому что пагинация и сортировка не влияют на количество записей.

### Примеры запросов

Посчитать все записи справочника:

```http
GET /api/v1/reference/dictionaries/outageKinds/count
```

Посчитать записи, найденные по строке поиска:

```http
GET /api/v1/reference/dictionaries/outageKinds/count?query=planned
```

Посчитать записи с теми же фильтрами, что применены в панели фильтров:

```http
GET /api/v1/reference/dictionaries/outageKinds/count?name=пла&code=planned&createdAtFrom=2026-02-06T21:00:00.000Z&createdAtTo=2026-05-31T20:59:59.999Z&updatedAtFrom=2026-02-06T21:00:00.000Z&updatedAtTo=2026-05-31T20:59:59.999Z
```

Пример с поисковой строкой на русском языке. Значение `query` должно быть URL-encoded:

```http
GET /api/v1/reference/dictionaries/outageKinds/count?query=пла
```

### Ответ 200

```json
{
  "total": 369,
  "byStatus": {
    "active": 357,
    "inactive": 12
  }
}
```

`total` - количество всех актуальных версий после применения фильтров.

### Ошибки

- `400 VALIDATION_ERROR` - некорректные фильтры, диапазоны дат или поисковая строка короче 3 символов.
- `404 DICTIONARY_NOT_FOUND` - справочник с указанным кодом не найден.

## **5. Добавление записи типового справочника**

Создает новую логическую запись справочника и первую строку-версию в таблице конкретного справочника.

```http
POST /api/v1/reference/dictionaries/{dictionaryCode}/items
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника из `ref_dictionaries.code`. |

### Тело запроса

```json
{
  "extId": 1,
  "code": "planned",
  "name": "Плановое"
}
```

### Правила

- `code` обязателен и должен быть уникален в рамках справочника.
- `name` обязателен.
- `extId` передается только для справочников, у которых `hasExtId = true`.
- Если найден дубль по `code` или `extId` среди актуальных версий этого справочника, запись не создается.
- Backend генерирует новый `itemId` логической записи и новый `id` строки-версии.
- Новая запись создается с `isActive = true`, `version = 1` и `isCurrent = true`.

### Ответ 201

```json
{
  "item": {
    "id": "b9fe8f18-a8e6-43a2-9dd5-2c134c879e50",
    "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
    "extId": 1,
    "code": "planned",
    "name": "Плановое",
    "isActive": true,
    "version": 1,
    "isCurrent": true,
    "createdAt": "2026-05-07T10:00:00.000Z",
    "updatedAt": "2026-05-07T10:00:00.000Z"
  }
}
```

### Ошибки

- `400 VALIDATION_ERROR` - не заполнены обязательные поля или неверный тип данных.
- `404 DICTIONARY_NOT_FOUND` - справочник не найден.
- `409 DICTIONARY_ITEM_DUPLICATE` - запись с таким `code` или `extId` уже существует.
- `422 DICTIONARY_NOT_EDITABLE` - справочник не разрешено редактировать.

## **6. Просмотр записи типового справочника**

Возвращает текущую версию логической записи справочника, то есть строку с isCurrent = true. Metadata для карточки просмотра возвращается отдельным endpoint `metadata`.

```http
GET /api/v1/reference/dictionaries/{dictionaryCode}/items/{itemId}
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника. |
| `itemId` | uuid | Идентификатор логической записи `item_id`. |

### Ответ 200

```json
{
  "item": {
    "id": "b9fe8f18-a8e6-43a2-9dd5-2c134c879e50",
    "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
    "extId": 1,
    "code": "planned",
    "name": "Плановое",
    "isActive": true,
    "version": 1,
    "isCurrent": true,
    "createdAt": "2026-05-07T10:00:00.000Z",
    "updatedAt": "2026-05-07T10:00:00.000Z"
  }
}
```

### Ошибки

- `404 DICTIONARY_NOT_FOUND` - справочник не найден.
- `404 DICTIONARY_ITEM_NOT_FOUND` - запись не найдена.

## **7. Получение списка версий записи**

Возвращает все строки-версии одной логической записи из таблицы конкретного справочника. Связь версий выполняется по `itemId`.

```http
GET /api/v1/reference/dictionaries/{dictionaryCode}/items/{itemId}/versions
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника из `ref_dictionaries.code`. |
| `itemId` | uuid | Идентификатор логической записи `item_id`. |

### Query-параметры

| Параметр | Тип | Обязательный | Описание |
| --- | --- | --- | --- |
| `page` | integer | нет | Номер страницы версий, начиная с `0`. По умолчанию `0`. |
| `size` | integer | нет | Количество версий на странице. По умолчанию `20`. |

По умолчанию версии возвращаются от новой к старой: `version,desc`.

### Ответ 200

```json
{
  "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
  "items": [
    {
      "id": "b9fe8f18-a8e6-43a2-9dd5-2c134c879e50",
      "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
      "extId": 1,
      "code": "planned",
      "name": "Плановое",
      "version": 1,
      "isActive": true,
      "isCurrent": true,
      "createdAt": "2026-05-07T10:00:00.000Z",
      "updatedAt": "2026-05-07T10:00:00.000Z"
    }
  ],
  "page": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

`createdAt` используется на вкладке `Версии` как дата создания версии. Состояние версии на вкладке `Версии` frontend определяет только по `isCurrent`: `true` означает актуальную версию, `false` - архивную. `isActive` отражает бизнес-активность значения справочника и не должен использоваться как состояние версии.

### Ошибки

- `404 DICTIONARY_NOT_FOUND` - справочник не найден.
- `404 DICTIONARY_ITEM_NOT_FOUND` - запись не найдена.

## **8. Просмотр конкретной версии записи**

Возвращает состояние записи на выбранной версии.

```http
GET /api/v1/reference/dictionaries/{dictionaryCode}/items/{itemId}/versions/{version}
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника из `ref_dictionaries.code`. |
| `itemId` | uuid | Идентификатор логической записи `item_id`. |
| `version` | integer | Номер версии записи. |

### Ответ 200

```json
{
  "item": {
    "id": "b9fe8f18-a8e6-43a2-9dd5-2c134c879e50",
    "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
    "extId": 1,
    "code": "planned",
    "name": "Плановое",
    "isActive": true,
    "version": 1,
    "isCurrent": true,
    "createdAt": "2026-05-07T10:00:00.000Z",
    "updatedAt": "2026-05-07T10:00:00.000Z"
  }
}
```

### Ошибки

- `404 DICTIONARY_NOT_FOUND` - справочник не найден.
- `404 DICTIONARY_ITEM_NOT_FOUND` - запись не найдена.
- `404 DICTIONARY_ITEM_VERSION_NOT_FOUND` - версия не найдена.

## **9. Изменение записи типового справочника**

Создает новую строку-версию записи в таблице конкретного справочника. Предыдущая актуальная версия переводится в `isCurrent = false`.

```http
PUT /api/v1/reference/dictionaries/{dictionaryCode}/items/{itemId}
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника из `ref_dictionaries.code`. |
| `itemId` | uuid | Идентификатор логической записи `item_id`. |

### Тело запроса

```json
{
  "code": "planned",
  "name": "Плановое отключение",
  "version": 1
}
```

### Правила

- `version` обязателен и должен совпадать с актуальной версией записи.
- При успешном изменении backend создает новую строку с новым `id`, тем же `itemId`, `version + 1` и `isCurrent = true`.
- Предыдущая актуальная строка того же `itemId` получает `isCurrent = false`.
- Проверка дублей выполняется по изменяемым уникальным полям, например по `code` среди актуальных версий других логических записей этого же справочника.
- Поля, которые не включены в `metadata.editForm.fields` как `editable = true`, не передаются в теле запроса и не изменяются этим методом. Для текущей структуры это как минимум `id`, `itemId`, `extId`, `createdAt`, `updatedAt`, `version`, `isCurrent`.
- Статус `isActive` не меняется этим эндпоинтом. Для смены статуса используется отдельный `PATCH`.

### Ответ 200

```json
{
  "item": {
    "id": "0a67f6df-9426-48f9-9d67-519daedc9b9d",
    "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
    "extId": 1,
    "code": "planned",
    "name": "Плановое отключение",
    "isActive": true,
    "version": 2,
    "isCurrent": true,
    "createdAt": "2026-05-07T10:10:00.000Z",
    "updatedAt": "2026-05-07T10:10:00.000Z"
  }
}
```

### Ошибки

- `400 VALIDATION_ERROR` - ошибка валидации.
- `404 DICTIONARY_NOT_FOUND` - справочник не найден.
- `404 DICTIONARY_ITEM_NOT_FOUND` - запись не найдена.
- `409 DICTIONARY_ITEM_DUPLICATE` - найден дубль по изменяемому уникальному полю, например по `code`.
- `409 DICTIONARY_ITEM_VERSION_CONFLICT` - передана неактуальная версия записи.
- `422 DICTIONARY_NOT_EDITABLE` - справочник не разрешено редактировать.

## **10. Изменение статуса записи типового справочника**

Активирует или деактивирует логическую запись справочника через создание новой строки-версии с измененным `isActive`.

```http
PATCH /api/v1/reference/dictionaries/{dictionaryCode}/items/{itemId}/status
```

### Path-параметры

| Параметр | Тип | Описание |
| --- | --- | --- |
| `dictionaryCode` | string | Код справочника из `ref_dictionaries.code`. |
| `itemId` | uuid | Идентификатор логической записи `item_id`. |

### Тело запроса

```json
{
  "isActive": false,
  "version": 2
}
```

### Правила

- `isActive` обязателен.
- `version` обязателен и должен совпадать с актуальной версией записи.
- При успешной смене статуса backend создает новую строку с новым `id` , тем же `itemId` , `version + 1` , новым `isActive` и `isCurrent = true` .
- Предыдущая актуальная строка того же `itemId` получает `isCurrent = false`.

### Ответ 200

```json
{
  "item": {
    "id": "cb9ee97c-d1f7-4d71-a17d-f3559c12f06f",
    "itemId": "1e0c0376-9ddd-4381-9167-cfe6b84bd157",
    "extId": 1,
    "code": "planned",
    "name": "Плановое отключение",
    "isActive": false,
    "version": 3,
    "isCurrent": true,
    "createdAt": "2026-05-07T10:20:00.000Z",
    "updatedAt": "2026-05-07T10:20:00.000Z"
  }
}
```

### Ошибки

- `400 VALIDATION_ERROR` - ошибка валидации.
- `404 DICTIONARY_NOT_FOUND` - справочник не найден.
- `404 DICTIONARY_ITEM_NOT_FOUND` - запись не найдена.
- `409 DICTIONARY_ITEM_VERSION_CONFLICT` - передана неактуальная версия записи.
- `422 DICTIONARY_NOT_EDITABLE` - справочник не разрешено редактировать.

## **11. Коды ошибок**

| HTTP | Код | Когда возникает |
| --- | --- | --- |
| `400` | `VALIDATION_ERROR` | Некорректное тело запроса, отсутствуют обязательные поля, неверные типы, некорректные фильтры или поисковая строка короче 3 символов. |
| `404` | `DICTIONARY_NOT_FOUND` | Не найден справочник по `dictionaryCode`. |
| `404` | `DICTIONARY_ITEM_NOT_FOUND` | Не найдена запись справочника. |
| `404` | `DICTIONARY_ITEM_VERSION_NOT_FOUND` | Не найдена версия записи. |
| `409` | `DICTIONARY_ITEM_DUPLICATE` | Дубль по уникальному полю справочника. Для создания это может быть `code` или `extId`, для изменения - только изменяемые поля, например `code`. |
| `409` | `DICTIONARY_ITEM_VERSION_CONFLICT` | Передана устаревшая версия записи. |
| `422` | `DICTIONARY_NOT_EDITABLE` | Справочник существует, но не доступен для редактирования. |
| `422` | `DICTIONARY_INACTIVE` | Справочник неактивен и не должен использоваться в пользовательском сценарии. |
