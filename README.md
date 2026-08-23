# To Sie Wypali

Small online shop for pottery products

online at:
tosiewypali.pl

## Stack

Java 22, Spring Boot, MySQL, React, Docker

## Requirements

- Running the app: Docker Desktop
- Running backend tests: JDK 22, Maven 3.9+ and Docker Desktop

## Run

Copy `.env.example` to `.env`, fill in the values and run:

Generate a JWT secret with `openssl rand -base64 32` and add the result as `JWT_SECRET` in `.env`.

```powershell
docker compose up --build
```

Open http://localhost:3000.

## Tests

```powershell
cd backend
mvn test
```

Tests run against a temporary MySQL database created by Testcontainers.
