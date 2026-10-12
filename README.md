GitHub Repository Searcher

A Spring Boot REST API that searches GitHub repositories using the GitHub Search API, stores repository information in PostgreSQL, updates existing records, and provides filtering and sorting capabilities.

## Features

- Search GitHub repositories using a search query.
- Filter searches by programming language.
- Sort GitHub search results by stars, forks, last updated date, and help-wanted issues.
- Persist repository details in PostgreSQL.
- Update existing repository records to avoid duplicate entries.
- Retrieve saved repositories using language and minimum-star filters.
- Validate request parameters and handle errors.
- Integrate with the GitHub REST API.

## Technology Stack

- Java 25
- Spring Boot
- Spring Web / REST API
- Spring Data JPA
- PostgreSQL
- Maven
- GitHub REST API
- Postman

## Prerequisites

Install the following before running the project:

- JDK 25
- PostgreSQL
- Maven, or use the Maven Wrapper included with the project
- Git
- Postman (optional, for API testing)

## Configuration

Configure your PostgreSQL connection in `src/main/resources/application.properties`.

Example:

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/freightfox_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
```

Create a PostgreSQL database named `freightfox_db` before starting the application.

Set the `DB_USERNAME` and `DB_PASSWORD` environment variables to your local PostgreSQL credentials. Do not commit database passwords or other secrets to GitHub.

## Running the Application

1. Clone the repository.
2. Open the project in Spring Tool Suite or another Java IDE.
3. Configure the PostgreSQL connection.
4. Ensure PostgreSQL is running and the database exists.
5. Run `GithubRepositorySearcherApplication.java` as a Spring Boot application.

The API will run at:

`http://localhost:8080`

## API Endpoints

### 1. Search GitHub Repositories

**Endpoint:** `POST /api/github/search`

Searches GitHub repositories and saves the returned repository details to PostgreSQL.

Request body:

json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}

Supported search sort values:

- `stars`
- `forks`
- `help-wanted-issues`
- `updated`

### 2. Retrieve Saved Repositories

Endpoint: `GET /api/github/repositories`

Retrieves repositories stored in PostgreSQL.

Supported query parameters:

- `language` — filter by programming language.
- `minStars` — return repositories with at least the specified number of stars.
- `sort` — sort saved results using the supported database sort options.

Example:

`GET /api/github/repositories?language=Java&minStars=100&sort=stars`

### 3. Search Repositories Using the Existing Search Endpoint

**Endpoint:** `GET /api/repositories/search?q=java`

Searches GitHub using the supplied query.

## Error Handling

The API handles invalid inputs and errors from GitHub, including connection failures and server-side failures. Invalid requests return an appropriate client error response.

## Database

Repository details are stored in the `github_repositories` table in PostgreSQL. Existing repository records are updated when the same GitHub repository is searched again.

## Testing

Use Postman to test the API endpoints, request validation, database persistence, filtering, and sorting.

## Author

Chandra Kumar
