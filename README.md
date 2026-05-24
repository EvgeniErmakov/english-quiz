# English Quiz

Backend для изучения английских слов и фразовых глаголов через Telegram.

Основной режим обучения:

> Бот присылает русское слово или фразу, а пользователь должен написать английское слово или фразовый глагол.

Пример:

```text
Bot: откладывать

User: put off

Bot: ✅ Верно. Следующее повторение: завтра.
```

Проект строится как система интервального повторения, а не как обычная викторина. Карточки должны появляться тогда, когда пользователь начинает их забывать.

## Идея проекта

Система помогает учить английские слова, выражения и фразовые глаголы по кривой забывания.

Пользователь получает карточку в Telegram, отвечает на неё, а backend сохраняет результат и пересчитывает дату следующего повторения.

## Главный сценарий

```text
1. Backend определяет, какую карточку пора повторить.
2. OpenClaw agent получает карточку из backend.
3. Telegram-бот отправляет пользователю русское слово.
4. Пользователь отвечает английским словом или фразовым глаголом.
5. OpenClaw agent отправляет ответ в backend.
6. Backend проверяет ответ.
7. При необходимости используется локальная LLM через Ollama.
8. Backend сохраняет результат.
9. Backend пересчитывает дату следующего повторения.
10. Telegram-бот показывает результат пользователю.
```

## Пример карточек

```text
RU: откладывать
EN: put off
```

```text
RU: выяснить
EN: find out
```

```text
RU: надёжный
EN: reliable
```

```text
RU: сдаться
EN: give up
```

## Архитектура

```text
Telegram
   ↓
OpenClaw
   ↓
English Cards Agent
   ↓             ↘
Backend API      Ollama
   ↓
PostgreSQL
```

## Роли компонентов

### Backend

Backend является главным источником истины.

Он отвечает за:

- хранение карточек;
- хранение пользователей;
- хранение прогресса;
- историю ответов;
- выбор карточек для повторения;
- алгоритм интервального повторения;
- статистику;
- API для OpenClaw agent.

### OpenClaw

OpenClaw используется как агентный слой между Telegram и backend.

Он отвечает за:

- получение сообщений из Telegram;
- отправку карточек пользователю;
- отправку ответов пользователя в backend;
- показ результата пользователю;
- вызов Ollama для объяснений и смысловой проверки, если это нужно.

### Ollama

Ollama используется для локального ИИ.

ИИ может помогать с:

- проверкой свободных ответов;
- объяснением ошибок;
- генерацией примеров;
- генерацией новых карточек;
- сравнением похожих слов.

ИИ не должен управлять расписанием повторений. Расписание повторений рассчитывает backend.

## Технологии

- Java 25
- Spring Boot 4
- Gradle
- PostgreSQL
- Flyway
- OpenClaw
- Ollama
- Telegram Bot

## Модель обучения

Карточки показываются по принципу интервального повторения.

Для ответа используется оценка:

```text
AGAIN — не вспомнил
HARD  — вспомнил с трудом
GOOD  — вспомнил нормально
EASY  — вспомнил легко
```

Эта оценка влияет на дату следующего повторения.

Пример:

```text
AGAIN → повторить скоро
HARD  → повторить через небольшой интервал
GOOD  → увеличить интервал
EASY  → сильно увеличить интервал
```

Цель системы — показать карточку не слишком рано и не слишком поздно, а примерно в момент, когда пользователь начинает её забывать.

## Основные сущности

### Card

Карточка хранит материал для изучения.

Пример полей:

```text
id
deckId
russianPrompt
englishAnswer
answerAliases
exampleSentence
exampleTranslation
partOfSpeech
level
tags
createdAt
updatedAt
```

### CardState

Состояние карточки для конкретного пользователя.

Пример полей:

```text
id
userId
cardId
difficulty
stability
retrievability
repetitions
lapses
lastReviewedAt
nextReviewAt
status
```

### ReviewAttempt

История конкретного ответа пользователя.

Пример полей:

```text
id
userId
cardId
userAnswer
expectedAnswer
normalizedUserAnswer
normalizedExpectedAnswer
isCorrect
rating
aiFeedback
responseTimeMs
reviewedAt
```

## Проверка ответа

Проверка ответа должна работать в несколько этапов:

```text
1. Нормализовать ответ пользователя.
2. Проверить точное совпадение с правильным ответом.
3. Проверить совпадение с алиасами.
4. При необходимости отправить ответ в Ollama.
5. Сохранить итоговый результат в backend.
```

Пример нормализации:

```text
"  Put   Off! " → "put off"
```

## Планируемые API

### Получить карточку для повторения

```http
GET /api/v1/agent/due-card
```

### Отправить ответ

```http
POST /api/v1/agent/submit-answer
```

Пример тела запроса:

```json
{
  "cardId": "card-id",
  "userAnswer": "put off",
  "responseTimeMs": 4300
}
```

### Отложить карточку

```http
POST /api/v1/agent/snooze
```

### Получить статистику

```http
GET /api/v1/agent/stats
```

## Локальный запуск

### Требования

- Java 25
- Gradle или Gradle Wrapper
- Docker
- Docker Compose

### Запуск PostgreSQL

Будет добавлен через `docker-compose.yml`.

Планируемый сервис:

```text
postgres
```

### Запуск backend

Linux/macOS:

```bash
./gradlew bootRun
```

Windows:

```bash
gradlew.bat bootRun
```

### Запуск тестов

Linux/macOS:

```bash
./gradlew test
```

Windows:

```bash
gradlew.bat test
```

### Сборка проекта

Linux/macOS:

```bash
./gradlew build
```

Windows:

```bash
gradlew.bat build
```

## Переменные окружения

Планируемые переменные:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

AGENT_API_KEY

OLLAMA_BASE_URL
OLLAMA_MODEL
```

Пример:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/english_quiz
SPRING_DATASOURCE_USERNAME=english_quiz
SPRING_DATASOURCE_PASSWORD=english_quiz

AGENT_API_KEY=change-me

OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3.1
```

## Этапы разработки

### Этап 1. Backend skeleton

- Spring Boot 4
- Java 25
- Gradle
- PostgreSQL
- Flyway
- базовая структура пакетов

### Этап 2. Карточки

- создание карточек;
- получение карточек;
- редактирование карточек;
- удаление карточек;
- алиасы ответов.

### Этап 3. Повторение

- выбор карточки, которую пора повторить;
- отправка ответа;
- сохранение попытки;
- пересчёт следующей даты повторения.

### Этап 4. Agent API

- endpoint для получения карточки;
- endpoint для отправки ответа;
- endpoint для статистики;
- защита agent API через API key.

### Этап 5. OpenClaw + Telegram

- подключить Telegram;
- настроить OpenClaw agent;
- научить агента брать карточки из backend;
- научить агента сохранять ответы в backend.

### Этап 6. Ollama

- подключить локальную LLM;
- проверять смысловые ответы;
- объяснять ошибки;
- генерировать примеры.

### Этап 7. Статистика

- сколько карточек повторено сегодня;
- процент правильных ответов;
- сложные карточки;
- карточки на завтра;
- streak.

## Принцип проекта

```text
Backend = память и расписание
OpenClaw = диалог и действия
Ollama = объяснение и смысловая проверка
Telegram = интерфейс
```
