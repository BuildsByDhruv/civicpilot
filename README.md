# CivicPilot

An agentic assistant that helps people find and apply for government welfare schemes.

## Tech stack
- Java 21, Spring Boot, Spring Data JPA
- PostgreSQL (pgvector) via Docker Compose

## Prerequisites
- Java 21
- Docker

## How to run
docker compose up -d
./mvnw spring-boot:run

Then open http://localhost:8080/health

## Roadmap
1. Data and intake API
2. Eligibility engine and explanations
3. Documents, RAG, and application guidance
4. Interface, deployment, and case study