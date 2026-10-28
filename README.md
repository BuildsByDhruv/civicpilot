# CivicPilot

An agentic assistant that helps people find and apply for government welfare schemes.

> **Status:** Week 1 (data and intake API) is complete. The eligibility engine is next.

## Tech stack
- Java 21, Spring Boot, Spring Data JPA
- PostgreSQL (pgvector) via Docker Compose

## Prerequisites
- Java 21
- Docker

## How to run

```bash
docker compose up -d
./mvnw spring-boot:run
```

Then open http://localhost:8080/health

### Loading the scheme data
On first run, set `civicpilot.seed.enabled=true` in `application.properties`. The seeders load 12 schemes, 55 eligibility rules and 40 field definitions from the CSV files in `data/`. They are safe to run again.

## API

### `POST /profiles`
Saves a user's intake answers and returns a profile ID. Every answer key must be a known field from `field_definition`. Skipped questions can be sent as `null` or left out.

```bash
curl -i -X POST http://localhost:8080/profiles \
  -H "Content-Type: application/json" \
  -d '{"answers": {"age": 25, "is_pregnant": true}}'
```

Success returns `201` with the new profile ID:

```json
{"id": "6e7669e6-7f3f-45b4-8dbb-3d7d8e0dbc07"}
```

Invalid input returns `400` and lists **all** problems at once:

```json
{"errors": ["Unknown field: height", "age: must be at most 120.0"]}
```

## Tests

```bash
./mvnw test
```

The validation rules for profile answers are covered by unit tests in `ProfileServiceTest`.

## Database schema

```mermaid
erDiagram
    scheme ||--o{ eligibility_rule : "has rules"
    scheme ||--o{ required_document : "needs documents"
    field_definition ||..o{ eligibility_rule : "field = field_key (no DB foreign key)"

    scheme {
        bigint id PK
        string code UK "e.g. PMVISH"
        string name
        string ministry
        string category
        text description
        string level
        string state "null for central schemes"
        string apply_mode
        string source_url
        date last_verified
        string status "DRAFT by default"
    }

    eligibility_rule {
        bigint id PK
        bigint scheme_id FK
        string field
        string operator
        text value "IN values separated by |"
        string rule_type "HARD or OPTIONAL"
        string rule_group
        boolean needs_verification
        string source_url
        text source_note
        date last_verified
    }

    required_document {
        bigint id PK
        bigint scheme_id FK
        string name
        boolean mandatory
    }

    field_definition {
        string field_key PK
        string value_type "NUMBER, BOOLEAN, TEXT or CHOICE"
        text question_text
        text allowed_values "CHOICE only, separated by |"
        double min_value
        double max_value
    }

    user_profile {
        uuid id PK
        timestamp created_at
        jsonb answers "skipped question = missing key"
    }
```

## Roadmap
1. Data and intake API (done)
2. Eligibility engine and explanations
3. Documents, RAG, and application guidance
4. Interface, deployment, and case study