# AGENTS.md

## Project overview

This project is an English learning backend for spaced-repetition flashcards.

The main learning direction is:

> Russian word or phrase → user must answer with the English word or phrasal verb.

Example:

- Prompt: `откладывать`
- Expected answer: `put off`

The system is not a generic quiz app. It is a memory-training system based on spaced repetition and the forgetting curve.

## Tech stack

- Java 25
- Spring Boot 4
- Gradle
- PostgreSQL
- Flyway
- Telegram integration through OpenClaw
- Local AI through Ollama

## Architecture principles

The backend is the source of truth.

The backend owns:

- cards
- decks
- user progress
- review history
- spaced-repetition scheduling
- answer result storage
- statistics
- agent-facing API

OpenClaw owns:

- Telegram conversation flow
- receiving user messages
- sending cards to the user
- calling backend APIs
- calling Ollama when semantic checking or explanations are needed

Ollama owns:

- checking semantically flexible answers
- explaining mistakes
- generating example sentences
- optionally generating new cards

Ollama must not own scheduling logic.

## Product rules

The current card mode is only:

> RU → EN

Do not implement EN → RU unless explicitly requested.

A card should represent one English target answer.

Examples of valid cards:

```text
RU: откладывать
EN: put off

RU: надёжный
EN: reliable

RU: выяснить
EN: find out
```

The expected English answer may be:

- a single word
- a phrasal verb
- a short fixed phrase

## Spaced repetition

The project should be designed around spaced repetition.

The scheduler should decide when a card becomes due based on the user's previous answers.

Use a scheduler abstraction:

```java
public interface ReviewScheduler {
    ReviewScheduleResult schedule(ReviewScheduleInput input);
}
```

Do not hardcode scheduling directly into controllers.

The first implementation may be simple, but the design should allow replacing it with a more advanced FSRS-like algorithm later.

The review rating model should be:

```text
AGAIN
HARD
GOOD
EASY
```

Avoid using only boolean `correct / incorrect` as the long-term memory signal.

A failed answer should usually bring the card back sooner.

An easy answer should increase the next interval.

## Domain model guidelines

Prefer these core entities:

- `User`
- `Deck`
- `Card`
- `CardState`
- `ReviewAttempt`

Suggested responsibility:

### Card

Stores the learning material.

Important fields:

- id
- deckId
- russianPrompt
- englishAnswer
- answerAliases
- exampleSentence
- exampleTranslation
- partOfSpeech
- level
- tags
- createdAt
- updatedAt

### CardState

Stores user-specific memory state for a card.

Important fields:

- id
- userId
- cardId
- difficulty
- stability
- retrievability
- repetitions
- lapses
- lastReviewedAt
- nextReviewAt
- status

### ReviewAttempt

Stores every user answer.

Important fields:

- id
- userId
- cardId
- userAnswer
- expectedAnswer
- normalizedUserAnswer
- normalizedExpectedAnswer
- isCorrect
- rating
- aiFeedback
- responseTimeMs
- reviewedAt

## Answer checking

Answer checking should have layers:

1. Normalize user input.
2. Compare against the expected answer and aliases.
3. If exact matching is not enough, optionally ask Ollama for semantic evaluation.
4. Persist the final result in the backend.

Normalization should handle:

- leading/trailing spaces
- repeated spaces
- case-insensitive matching
- punctuation where reasonable

Do not make Ollama the only source of correctness.

Exact answer and aliases should be checked first.

## API design

Prefer versioned APIs:

```text
/api/v1/...
```

Prefer a separate agent-facing API:

```text
/api/v1/agent/due-card
/api/v1/agent/submit-answer
/api/v1/agent/snooze
/api/v1/agent/stats
```

The OpenClaw agent should not access the database directly.

## Security rules

The agent API must be protected.

Use an API key or token for OpenClaw-to-backend communication.

Do not expose dangerous bulk operations to the agent.

Avoid endpoints such as:

```text
DELETE /api/v1/cards/all
DELETE /api/v1/users/all
```

All agent calls that change learning state should be logged.

## Coding style

Use clean Spring Boot architecture:

```text
controller
service
domain
repository
dto
config
```

Controllers should be thin.

Business logic belongs in services.

Scheduling logic belongs in scheduler classes.

Persistence logic belongs in repositories.

Use constructor injection.

Avoid field injection.

Prefer records for immutable DTOs.

Use meaningful names.

Do not over-engineer abstractions before they are needed.

## Database

Use Flyway migrations.

Do not rely on Hibernate auto-DDL for production schema creation.

Migration files should be placed in:

```text
src/main/resources/db/migration
```

Migration naming:

```text
V1__init_schema.sql
V2__add_review_attempts.sql
V3__add_card_aliases.sql
```

## Testing

Add tests for important business logic.

Priority test areas:

- answer normalization
- exact answer checking
- alias matching
- review scheduling
- next review time calculation
- due card selection

Run before completing code changes:

```bash
./gradlew test
```

On Windows:

```bash
gradlew.bat test
```

## Commands

Build:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

Run application:

```bash
./gradlew bootRun
```

On Windows:

```bash
gradlew.bat build
gradlew.bat test
gradlew.bat bootRun
```

## README policy

Keep README.md updated when adding:

- new environment variables
- new endpoints
- new Docker services
- new setup steps
- new architectural decisions

## Current priority

Implement the backend first.

The preferred order is:

1. Domain model
2. Database migrations
3. CRUD for cards
4. Due-card selection
5. Submit-answer flow
6. Basic scheduler
7. Agent API
8. Ollama integration
9. OpenClaw Telegram integration
10. Statistics
