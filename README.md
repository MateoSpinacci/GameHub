# GameHub 🎮

**GameHub** is a Java and Spring Boot backend project for managing a personal video game library. It uses a microservices architecture to combine game information from the [RAWG Video Games Database API](https://rawg.io/apidocs) with per-user library data stored locally, secured with **Keycloak** (OAuth2 / JWT).

> **Project status:** Work in progress — a learning and portfolio project focused on backend development with Spring Boot.

## Features

- Search for video games by name using RAWG.
- Retrieve details for a specific game.
- Authenticate with Keycloak and call the library API with a JWT (`ms-library` acts as an OAuth2 Resource Server).
- Each user has their **own** library: entries are linked to the token's `sub` claim and are never visible to other users.
- Role-based access control: `USER` and `ADMIN` can manage their library, only `ADMIN` can delete entries.
- Add games to the library after verifying that they exist (duplicates per user are rejected).
- List saved games and retrieve an individual library entry, enriched with game information from `ms-games`.
- Update a game's status and personal rating, or remove it.
- Validate incoming requests and handle errors consistently (`ProblemDetail` in `ms-games`, `ResponseStatusException` in the other services).
- Access the microservices through an API Gateway.

## Architecture

```text
                    Client / Postman
                      |          \
                      |           \  (get token)
                      v            v
                 API Gateway     Keycloak (:8180)
                  /       \          ^
                 v         v         | validates JWT (issuer-uri)
          ms-games     ms-library ---+
              |         |      \
              v         v       \
           RAWG API  H2 (JPA)    \
              ^                   \
              |____________________\
               ms-library calls ms-games
               to validate and enrich games
```

| Module | Port | Responsibility |
| --- | --- | --- |
| `Gateway` | 8080 (default) | Routes `/api/games/**` and `/api/library/**` to the right microservice. |
| `ms-games` | 8081 | Integrates with RAWG to search for games and retrieve their details. Handles RAWG errors (404 / 502). |
| `ms-library` | 8082 | Manages each user's library with Spring Data JPA, validates JWTs with Spring Security and calls `ms-games` when game information is needed. |
| `ms-users` | 8083 | Standalone user CRUD (H2, BCrypt-hashed passwords). **Experimental:** it is not routed through the Gateway and is not yet integrated with Keycloak. |
| `keycloak` + `keycloak-db` | 8180 | Identity provider and its PostgreSQL database, started with `docker-compose.yml`. |

### Request flow

When a client adds a game, `ms-library` reads the user ID from the JWT, checks that the game is not already in that user's library, asks `ms-games` whether the RAWG game exists, and then saves the entry to H2.

When a client retrieves library entries, `ms-library` combines the locally stored fields with game details requested from `ms-games`. The database does **not** duplicate game titles or images from RAWG.

If `ms-games` is unreachable or fails, `ms-library` answers with `503 Service Unavailable` or `502 Bad Gateway` instead of a generic 500.

## Security

- **Identity provider:** Keycloak 26 (realm `Gamehub`), run locally with Docker Compose.
- **`ms-library`** validates tokens against `http://localhost:8180/realms/Gamehub` and maps Keycloak **realm roles** (`realm_access.roles`) to Spring authorities (`ROLE_USER`, `ROLE_ADMIN`).
- The API is **stateless** (no sessions) and CSRF is disabled, as is usual for token-based APIs.

| Endpoint | Required role |
| --- | --- |
| `DELETE /api/library/{id}` | `ADMIN` |
| any other `/api/library/**` | `USER` or `ADMIN` |

> **Current limitations:** `ms-games` and the Gateway do not validate tokens yet (the Gateway just forwards the `Authorization` header to `ms-library`). The H2 console is open in `ms-library` for development only.

## Tech stack

- **Java 17**, **Spring Boot 4.1** and **Spring Web MVC**
- **Spring Security** (OAuth2 Resource Server, JWT)
- **Keycloak** + **PostgreSQL** (Docker Compose)
- **Spring Data JPA** / **Hibernate**
- **Spring Cloud Gateway** (WebFlux)
- **Spring RestClient** for HTTP communication (with timeouts in `ms-library`)
- **H2 Database** (in-memory, for development)
- **JUnit 5** and **Mockito** for service tests (`ms-library`, `ms-users`)
- **Maven** (multi-module)
- **Postman** for manual API testing

## API endpoints

Calls go through the Gateway (`http://localhost:8080`). Library endpoints require `Authorization: Bearer <access_token>`.

### Games service

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/games?nombre={name}` | Search for games by name. |
| `GET` | `/api/games/{id}` | Retrieve a game by its RAWG ID. |

### Library service

| Method | Endpoint | Role | Description |
| --- | --- | --- | --- |
| `POST` | `/api/library` | USER / ADMIN | Add a game to your library. |
| `GET` | `/api/library` | USER / ADMIN | Retrieve your entries, enriched with game details. |
| `GET` | `/api/library/{id}` | USER / ADMIN | Retrieve one of your entries by its local ID. |
| `PATCH` | `/api/library/{id}` | USER / ADMIN | Update an entry's status and/or personal rating. |
| `DELETE` | `/api/library/{id}` | ADMIN | Remove an entry. |

> **ID distinction:** `id` identifies a library entry in H2, while `gameId` identifies the game in RAWG.

### Example: add a game

```http
POST /api/library
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "gameId": 22509,
  "status": "PLAYING",
  "personalRating": 9
}
```

A successful creation returns **201 Created** with the saved entry. Valid statuses are `WANT_TO_PLAY`, `PLAYING`, `COMPLETED`, and `DROPPED`; `personalRating` goes from 1 to 10. Adding the same game twice returns **409 Conflict**.

## Running locally

### Prerequisites

- JDK 17
- Maven (or the Maven Wrapper included in each module)
- Docker and Docker Compose (for Keycloak)
- A [RAWG API key](https://rawg.io/apidocs)

### 1. Clone the repository

```bash
git clone https://github.com/MateoSpinacci/GameHub.git
cd GameHub
```

### 2. Configure the RAWG API key

`ms-games` reads the key from the `RAWG_API_KEY` environment variable (`rawg.api.key=${RAWG_API_KEY}`). Set it in your shell or IDE run configuration. Never commit the actual key.

### 3. Start Keycloak

```bash
docker compose up -d
```

Keycloak will be available at <http://localhost:8180> (dev admin: `admin` / `admin`, override with `KC_ADMIN_USER` and `KC_ADMIN_PASSWORD`; these are development credentials only).

Then, in the admin console:

1. Create the realm **`Gamehub`**.
2. Create the realm roles **`USER`** and **`ADMIN`**.
3. Create a client for testing and a user with one of those roles.

### 4. Start the applications

Run each Spring Boot application from your IDE, or use Maven in separate terminals:

```bash
cd ms-games   && mvn spring-boot:run
cd ms-library && mvn spring-boot:run
cd Gateway    && mvn spring-boot:run
```

### 5. Test the API

1. Request an access token from Keycloak (`/realms/Gamehub/protocol/openid-connect/token`).
2. Call the endpoints through the Gateway with `Authorization: Bearer <token>`.

You can also call `ms-games` and `ms-library` directly on their ports for debugging.

### Run the tests

```bash
mvn test
```

> **Note:** H2 runs in memory, so library entries are lost when `ms-library` stops.

## Current limitations and planned improvements

- [x] Add unit tests with JUnit 5 and Mockito (services).
- [x] Authenticate with Keycloak and isolate each user's library.
- [x] Handle errors when `ms-games` or RAWG are unavailable.
- [ ] Add integration tests (controllers + security).
- [ ] Validate JWTs in the Gateway and `ms-games` as well.
- [ ] Decide the role of `ms-users` (remove it or sync it with Keycloak users).
- [ ] Optimize library enrichment to avoid one sequential HTTP request per saved game.
- [ ] Move from in-memory H2 to persistent PostgreSQL storage.
- [ ] Containerize the services with Docker.

## What I'm learning

This project is an opportunity to practice REST API design, layered architecture, dependency injection, DTO mapping, Spring Data JPA, inter-service HTTP communication, validation, error handling, API Gateway routing, and securing microservices with OAuth2, JWT and Keycloak.

---

Built as a hands-on Java backend learning project. Contributions and feedback are welcome.