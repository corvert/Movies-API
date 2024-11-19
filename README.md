

---

# Movies API

## Overview
The Movies API is a RESTful service designed to manage a movie database for a 
local film society. The API allows the society to easily organize and access 
information about movies, genres, and actors, as well as to manage these entities 
and their relationships. Built with **Spring Boot** and **JPA**, it supports CRUD 
operations, complex entity relationships, filtering, and basic error handling. 
The project uses **SQLite** for persistence and provides endpoints for efficient 
data retrieval, including filtering by genre, release year, and actor.

## Features
- CRUD operations for movies, genres, and actors
- Many-to-many relationships between movies and genres, and movies and actors
- Filtering and search endpoints for streamlined data retrieval
- Basic pagination and search functionalities
- Error handling and input validation for a robust API experience
- Soft and forced deletion with relational constraints

## Setup and Installation

### Prerequisites
- **Java** (version 11 or higher)
- **Maven** (for managing project dependencies)
- **SQLite** (for the database)

### Initial Setup
1. Clone the repository:
   ```bash
   git clone https://gitea.kood.tech/orvetpriimagi/kmdb
   ```
2. Navigate to the project directory:
   ```bash
   cd movies-api
   ```
3. Run **Spring Initializr** with dependencies for Spring Web and Spring Data JPA if starting from scratch.
4. Update your `application.properties` to configure the SQLite database:
   ```properties
   spring.datasource.url=jdbc:sqlite:movies.db
   spring.datasource.driver-class-name=org.sqlite.JDBC
   spring.jpa.hibernate.ddl-auto=update
   ```
5. Add the **SQLite JDBC driver** dependency to your `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.xerial</groupId>
       <artifactId>sqlite-jdbc</artifactId>
       <version>3.36.0.3</version>
   </dependency>
   ```

### Building the Project
To build the project, run:
```bash
mvn clean install
```

### Running the Application
To start the server, run:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Usage Guide

### CRUD Operations and Filtering

#### Genres
- **Create a Genre:** `POST /api/genre/add-genre`
  - #### Example JSON for POST 
```json
{
   "genreName": "Action"
}
```

- **Get All Genres:** `GET /api/genre`
- **Get Genre by ID:** `GET /api/genre/{id}`
- **Update Genre:** `PATCH /api/genre/update/{genreId}`
  - #### Example JSON for PATCH
```json
{
   "genreName": "Action"
}
```
- **Delete Genre:** `DELETE /api/genre/{genreId}` (with `?force=true` for forced deletion)

#### Movies
- **Create a Movie:** `POST /api/movie/add-movie`
  - #### Example JSON for POST 
```json
{
  "movieTitle": "Inception",
  "releaseYear": 2010,
  "duration": 148,
  "genreSet": [
     {"genreName":  "Action Epic"},
     {"genreName":  "Adventure Epic"},
     {"genreName":  "Psychological Thriller"},
     {"genreName":  "Sci-Fi Epic"},
     {"genreName":  "Action"},
     {"genreName":  "Adventure"},
     {"genreId":  11}
  ],
  "actorSet": [
     {"actorName":  "Leonardo DiCaprio", "birthDate":  "1974-11-11"},
     {"actorName":  "Joseph Gordon-Levitt", "birthDate":  "1981-02-17"},
     {"actorName":  "Elliot Page", "birthDate":  "1987-02-21"},
     {"actorName":  "Ken Watanabe", "birthDate":  "1959-10-21"},
     {"actorName":  "Tom Hardy", "birthDate":  "1977-09-15"},
     {"actorName":  "Dileep Rao", "birthDate":  "1973-07-29"},
     {"actorId":  12}
  ]
}
```
- **Get All Movies:** `GET /api/movie`
- **Get Movie by ID:** `GET /api/movie/{movieId}`
- **Filter by Genre:** `GET /api/movie/movies/{genreId}`
- **Filter by Release Year:** `GET /api/movie/year/{releaseYear}`
- **Search by Title:** `GET /api/movie/search/{someName}`
- **Get Actors in a Movie:** `GET /api/movie/{movieId}/actors`
- **Update Movie:** `PATCH /api/movie/update/{movieId}`
-   - #### Example JSON for PATCH
```json
{
  "movieTitle": "Inception",
  "releaseYear": 2010,
  "duration": 148,
  "genreSet": [
     {"genreName":  "Action Epic"},
     {"genreName":  "Adventure Epic"},
     {"genreName":  "Psychological Thriller"},
     {"genreName":  "Sci-Fi Epic"},
     {"genreName":  "Action"},
     {"genreName":  "Adventure"},
     {"genreId":  11}
  ],
  "actorSet": [
     {"actorName":  "Leonardo DiCaprio", "birthDate":  "1974-11-11"},
     {"actorName":  "Joseph Gordon-Levitt", "birthDate":  "1981-02-17"},
     {"actorName":  "Elliot Page", "birthDate":  "1987-02-21"},
     {"actorName":  "Ken Watanabe", "birthDate":  "1959-10-21"},
     {"actorName":  "Tom Hardy", "birthDate":  "1977-09-15"},
     {"actorName":  "Dileep Rao", "birthDate":  "1973-07-29"},
     {"actorId":  12}
  ],
  "actorsToRemove": [
    {"actorId": 10}

  ],

  "genresToRemove": [
    {"genreId": 22}
  ]
}
```
- **Delete Movie:** `DELETE /api/movie/{movieId}` (with `?force=true` for forced deletion)

#### Actors
- **Create an Actor:** `POST /api/actor/add-actor`
  - #### Example JSON for POST
```json
{
   "actorName": "Chris Evans",
   "birthDate": "1981-06-13"
}
```
- **Get All Actors:** `GET /api/actor`
- **Get Actor by ID:** `GET /api/actor/{actorId}`
- **Filter by Name:** `GET /api/actor/search/{name}`
- **Get Movies for an Actor:** `GET /api/actor/{actorId}/movies`
- **Update Actor:** `PATCH /api/actor/update/{actorId}`
  - #### Example JSON for PATCH
```json
{
   "actorName": "Chris Evans jr.",
   "birthDate": "1981-06-13"
}
```
- **Delete Actor:** `DELETE /api/actor/{actorId}` (with `?force=true` for forced deletion)


### Error Handling
The API includes basic validation and custom error messages:
- **ResourceNotFoundException:** For missing entities
- **Validation Errors:** Enforced through Bean Validation annotations (e.g., `@NotNull`, `@Past`)

Global error handling is implemented with `@ControllerAdvice` to return clear, structured error messages with appropriate HTTP status codes (e.g., `404 Not Found`, `400 Bad Request`).





## Additional Notes
- **Soft Delete:** Attempts to delete resources with existing relationships (such as genres with movies or actors in movies) will result in a `400 Bad Request` with a clear message. Use `force=true` to override.
- **Pagination:** Use `page` (default 0) and `size` (default 10) parameters on any endpoint returning multiple results, e.g., `/api/movies?page=0&size=10`.




---
