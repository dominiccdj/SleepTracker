# SleepTracker

A RESTful API service for tracking sleep data and generating sleep statistics.

## Overview

Sleep Logger API allows users to log their sleep patterns, including bed time, wake time, and how they felt in the morning. The application provides endpoints for creating sleep logs, retrieving individual logs, and generating statistical reports such as 30-day averages.

## Features

- Create and manage user profiles
- Log sleep data (bed time, wake time, morning feeling)
- Retrieve the most recent sleep log
- View 30-day sleep statistics and averages
- Calculate average time in bed, bed times, and wake times
- Track morning feeling trends (BAD, OK, GOOD)

## Technology Stack

- Kotlin 1.6.21
- Spring Boot 2.7.17
- PostgreSQL for data persistence
- Flyway for database migrations
- SpringDoc OpenAPI for API documentation
- Docker for containerization

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Git

### Installation and Running

1. Clone the repository:

```
git clone https://github.com/dominiccdj/SleepTracker.git
cd SleepTracker
```

2. Build and run the application using Docker Compose:

```
docker-compose up --build
```

3. Access the API at `http://localhost:8080`.

### API Documentation
The API documentation is available via Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Key Endpoints

#### Users

- `POST /api/users` - Create a new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users


#### Sleep Logs

- `POST /api/sleep-logs` - Create a new sleep log
- `GET /api/sleep-logs/users/{userId}/last-night` - Get the most recent sleep log for a user
- `GET /api/sleep-logs/users/{userId}` - Get all sleep logs for a user
- `GET /api/sleep-logs/users/{userId}/averages/30-day` - Get 30-day sleep statistics


## Example Requests

### Create a User

```json
POST /api/users
{
  "username": "johndoe",
  "email": "john.doe@example.com"
}
```


### Create a Sleep Log

```json
POST /api/sleep-logs
{
  "bedTime": "2025-05-05T22:30:00",
  "wakeTime": "2025-05-06T06:45:00",
  "morningFeeling": "GOOD",
  "userId": 1
}
```


### Get 30-Day Averages

```
GET /api/sleep-logs/users/1/averages/30-day
```

Response:

```json
{
  "startDate": "2025-04-08",
  "endDate": "2025-05-08",
  "averageTimeInBedMinutes": 480.0,
  "averageBedTime": "22:42",
  "averageWakeTime": "06:42",
  "morningFeelingFrequencies": {
    "GOOD": 15,
    "OK": 10,
    "BAD": 5
  }
}
```

## API Testing with Postman

A Postman collection is included in this repository to help you quickly test the Sleep Logger API. The collection contains pre-configured requests for all available endpoints.

### Using the Postman Collection

1. Download and install [Postman](https://www.postman.com/downloads/)
2. Import the collection:
    - Click "Import" in Postman
    - Select the `SleepTracker.postman_collection.json` file from this repository
3. Start the application using Docker Compose
4. Execute the requests in the following recommended order:
    - Create a new user
    - Create a new sleep log (using the user ID from the previous response)
    - Get all sleep logs for a user
    - Get the most recent sleep log for a user
    - Get 30-day sleep statistics

The collection includes requests for all API endpoints:

- User management (create, get by ID, get all)
- Sleep log creation and retrieval
- Sleep statistics and averages

This makes it easy to test the application without writing any code or using command-line tools like curl.