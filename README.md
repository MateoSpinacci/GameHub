# GameHub 🎮

**GameHub** is a Java and Spring Boot backend project for managing a personal video game library. It uses a microservices architecture to combine game information from the [RAWG Video Games Database API](https://rawg.io/apidocs) with personal library data stored locally.

> **Project status:** Work in progress — a learning and portfolio project focused on backend development with Spring Boot.

## Features

- Search for video games by name using RAWG.
- Retrieve details for a specific game.
- Add games to a personal library after verifying that they exist.
- List saved games and retrieve an individual library entry.
- Enrich library GET responses with game information from the games microservice.
- Update a game's status and personal rating.
- Remove games from the library.
- Validate incoming requests and handle common HTTP errors.
- Access both microservices through an API Gateway.

## Architecture

```text
                         Client / Postman
                               |
                               v
                         API Gateway
                          /         \
                         v           v
                  ms-games         ms-library
                      |             |       \
                      v             v        \
                   RAWG API      H2 Database  |
                      ^                       |
                      |_______________________|
                      ms-library calls ms-games
                      to validate and enrich games
```

| Module | Responsibility |
| --- | --- |
| `api-gateway` | Routes incoming requests to the appropriate microservice. |
| `ms-games` | Integrates with RAWG to search for games and retrieve their details. |
| `ms-library` | Manages personal library entries with Spring Data JPA and calls `ms-games` when game information is needed. |

### Request flow

When a client adds a game, `ms-library` checks for an existing library entry, asks `ms-games` whether the RAWG game exists, and then saves the personal library data to H2.

When a client retrieves library entries, `ms-library` combines locally stored fields with game details requested from `ms-games`. The database does **not** duplicate game titles or images from RAWG.

## Tech stack

- **Java**
- **Spring Boot** and **Spring Web**
- **Spring Data JPA** / **Hibernate**
- **Spring Cloud Gateway**
- **Spring RestClient** for HTTP communication
- **H2 Database** (in-memory, for development)
- **Maven**
- **Postman** for manual API testing

## API endpoints

### Games service

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/games?nombre={name}` | Search for games by name. |
| `GET` | `/api/games/{id}` | Retrieve a game by its RAWG ID. |

### Library service

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/library` | Add a game to the library. |
| `GET` | `/api/library` | Retrieve all library entries, enriched with game details. |
| `GET` | `/api/library/{id}` | Retrieve an enriched library entry by its local ID. |
| `PATCH` | `/api/library/{id}` | Update a library entry's status and/or personal rating. |
| `DELETE` | `/api/library/{id}` | Remove a library entry. |

> **ID distinction:** `id` identifies a library entry in H2, while `gameId` identifies the game in RAWG.

### Example: add a game

```http
POST /api/library
Content-Type: application/json
```

```json
{
  "gameId": 22509,
  "status": "PLAYING",
  "personalRating": 9
}
```

A successful creation returns **201 Created** with the saved library entry. The library also supports the statuses `WANT_TO_PLAY`, `PLAYING`, `COMPLETED`, and `DROPPED`.

### Example: retrieve the library

```http
GET /api/library
```

Library GET responses combine the saved entry (including status and personal rating) with game details retrieved from `ms-games`.

## Running locally

### Prerequisites

- A compatible JDK for the project's Maven configuration
- Maven (or the Maven Wrapper, if included in the module)
- A [RAWG API key](https://rawg.io/apidocs)

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/GameHub.git
cd GameHub
```

Replace `YOUR_USERNAME` with your GitHub username.

### 2. Configure the RAWG API key

Keep API credentials out of version control. Configure `ms-games` to read the key from an environment variable, for example:

```properties
rawg.api.key=${RAWG_API_KEY}
```

Set `RAWG_API_KEY` in your local environment or IDE run configuration. Never commit the actual key.

### 3. Start the applications

Run each Spring Boot application from your IDE, or use Maven in separate terminals:

```bash
cd ms-games
mvn spring-boot:run
```

```bash
cd ms-library
mvn spring-boot:run
```

```bash
cd api-gateway
mvn spring-boot:run
```

Default service ports used in this project:

- `ms-games`: **8081**
- `ms-library`: **8082**
- `api-gateway`: use the port configured in its `application.properties` / `application.yml`.

The Gateway routes `/api/games/**` to `ms-games` and `/api/library/**` to `ms-library`.

### 4. Test the API

Use Postman (or another HTTP client) to call the endpoints through the Gateway. You can also call `ms-games` and `ms-library` directly on their local ports for debugging.

> **Note:** H2 currently runs in memory, so saved library entries are lost when the library service stops.

## Current limitations and planned improvements

- [ ] Add automated unit and integration tests with JUnit 5 and Mockito.
- [ ] Improve resilience when game information cannot be retrieved from another service.
- [ ] Optimize library enrichment to avoid one sequential HTTP request per saved game.
- [ ] Move from in-memory H2 to persistent PostgreSQL storage.
- [ ] Containerize the services with Docker.

## What I'm learning

This project is an opportunity to practice REST API design, layered architecture, dependency injection, DTO mapping, Spring Data JPA, inter-service HTTP communication, validation, error handling, and API Gateway routing.

---

Built as a hands-on Java backend learning project. Contributions and feedback are welcome.
