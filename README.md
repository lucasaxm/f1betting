# Formula 1 Betting Service

A Spring Boot API that lets you:

1. **Browse F1 events** with each driver’s odds.
2. **Place a bet** on a driver.
3. **Declare an event outcome** and automatically settle all bets.

## Stack

* **Language & Framework:** Java 21, using Spring Boot 3 (Spring Web, Spring Data JPA, Spring Validation, etc.). The
  service is a RESTful API built with Spring’s annotation-driven approach.
* **Database:** PostgreSQL 15 for persistence (with Flyway for schema migrations).
* **Build & Tools:** Gradle (Wrapper included) for building/testing, Docker Compose for running a Postgres container.
  Lombok is used to reduce boilerplate.
* **Testing:** JUnit 5 with Spring Boot Test (MockMvc for web layer tests). An **embedded Postgres** database is used
  for integration tests (via the Zonky library) and OkHttp’s **MockWebServer** is used to simulate the external F1 API
  in tests.

## How to Run

### Dev mode

```bash
./gradlew bootRun
```

*Gradle’s Docker Compose plugin* starts Postgres automatically; the service starts on *
*[http://localhost:8080](http://localhost:8080)** with the DB already provisioned.

### Using the packaged JAR
If you prefer to run the service as a standalone JAR, you can build it and provision the database using Docker Compose.
```bash
./gradlew build
docker compose up -d
java -jar build/libs/f1betting-0.0.1-SNAPSHOT.jar
```

### Running tests

```bash
./gradlew clean test
```

The tests will spawn an embedded database and do not require external API access (they use MockWebServer).

## API

Swagger UI is available at **`/swagger-ui.html`** (or `/swagger-ui/index.html`) once the app is running. Feel free to
explore the API there. Below are the key endpoints and their usage.

### List Events

**Request:** `GET /v1/events` with optional query parameters:

* `year` – filter by season year (e.g., 2023)
* `type` – Filter by event type
    * Event types: `PRACTICE`, `QUALIFYING`, `RACE`
* `country` – filter by country name (e.g., `Italy`)
* `page` - Zero-based page index (default `0`)
* `size` - Page size (default `20`, max `100`)
* `sort`- Sorting criteria `property,(asc|desc)` (e.g., `sort=date_start,desc`).
    * Allowed properties: `year`, `date_start`, `event_name`, `country_name`.

**Response:** Returns a paginated list of events with their driver odds. Example:

```json
{
  "page": 0,
  "size": 10,
  "total_elements": 1,
  "total_pages": 1,
  "last": true,
  "content": [
    {
      "id": "d4f1...b3e9",
      "event_name": "Belgian Grand Prix",
      "event_type": "RACE",
      "year": 2024,
      "country_name": "Belgium",
      "date_start": "2024-08-25T14:00:00Z",
      "winner": null,
      "odds": [
        {
          "id": "a890...341e",
          "value": 2,
          "created_at": "2024-08-01T12:00:00Z",
          "driver": {
            "id": "c1e2...9fe4",
            "full_name": "Driver One"
          }
        }
      ]
    }
  ]
}
```

Each event includes a list of `odds` for every participating driver (with a randomly assigned odd of 2, 3, or 4).

### Place a Bet

**Request:** `POST /v1/bets`
**Body:** JSON with the user, chosen odds, and amount. For example:

```json
{
  "user_id": "8c215643-ddbb-4cbe-8d92-fbd93f09018a",
  "event_odd_id": "a890...341e",
  "amount": 25.00
}
```

(This represents User 1 betting €25 on the driver associated with the given `event_odd_id`.)

**Response:** HTTP 201 Created, with bet details and updated balance. Example:

```json
{
  "bet_id": "05c26a8e-...-91aa",
  "status": "PENDING",
  "event_id": "d4f1...b3e9",
  "driver_id": "c1e2...9fe4",
  "odd": 2,
  "amount": 25.0,
  "remaining_balance": 75.0,
  "created_at": "2025-07-31T07:36:58Z"
}
```

The user’s `remaining_balance` is decreased by the bet amount (started at €100).

### Declare Outcome

**Request:** `POST /v1/events/{event_id}/outcome`
**Body:** JSON specifying the winner’s driver ID. For example:

```json
{
  "winner_driver_id": "c1e2...9fe4"
}
```

**Response:** Marks the event as finished and settles all bets on that event. Example:

```json
{
  "event_id": "d4f1...b3e9",
  "winner_driver_id": "c1e2...9fe4",
  "bets_settled": 1,
  "total_paid": 50.0
}
```

In this scenario, one bet was settled. `total_paid` indicates the total payout (in EUR) to winning bets. Each winning
bet pays `amount * odd` (the original stake is effectively returned via this payout). Losing bets remain deducted.

### Users

These endpoints are here so we can see the current balance and bets of each user without having to query the database
directly. They were not required in the original assignment but are useful for testing and debugging.

**Request:** `GET /v1/users`

**Response:** Returns a list of users with their current balance. Example:

```json
[
  {
    "id": "8c215643-ddbb-4cbe-8d92-fbd93f09018a",
    "name": "User 1",
    "balance": 75.0
  }
]
```

**Request:** `GET /v1/users/{user_id}`

**Response:** Returns a specific user’s details, including their bets. Example:

```json
{
  "id": "8c215643-ddbb-4cbe-8d92-fbd93f09018a",
  "name": "User 1",
  "balance": 75.0,
  "bets": [
    {
      "bet_id": "05c26a8e-...-91aa",
      "status": "PENDING",
      "event_id": "d4f1...b3e9",
      "driver_id": "c1e2...9fe4",
      "odd": 2,
      "amount": 25.0,
      "created_at": "2025-07-31T07:36:58Z"
    }
  ]
}
```

## Assumptions & Trade-offs

* **Event Odds ID to Prevent Stale Bets:** The client must specify an `event_odd_id` when placing a bet. This ensures
  the bet is tied to a specific odds entry from the list-events response. The system verifies that the provided odds
  entry is the latest for that event (via a DB view); if not, the bet is rejected as using outdated odds.
* **Pre-created Users (No Auth):** For simplicity, user accounts are pre-seeded in the database with an initial balance
  of €100. The API expects a `user_id` in requests (no authentication token). There’s no endpoint to create users or log
  in.
* **Fixed Bankroll:** Users cannot top up their balance or withdraw money. The balance only changes by placing bets or
  winning outcomes, in line with the assignment’s condition of a fixed €100 starting bankroll.
* **Randomized Odds, Refreshed Each Sync:** Odds for each driver/event are generated randomly (2, 3, or 4) whenever
  events are synchronized. The latest odds are stored, but if the system syncs data again for an upcoming event, it will
  create new odds entries (simulating odds updates in a live betting market). This means the same event could have
  different odds at different times, and only the current odds are offered for new bets.
* **Minimal Initial Data Load:** On startup, the service only fetches a limited set of events (by default, the current
  season’s events, configurable via `min_year`). This trade-off keeps startup time fast and avoids hitting rate limits
  by not pulling an entire historical dataset. If a client requests an event from a year that hasn’t been cached, the
  system will fetch and cache that year on-demand.

## Key Design Decisions

* **Transactional Consistency with Locks:** Critical operations use transactions and locks to maintain consistency. For
  example, when placing a bet, the code locks the user’s row (to safely update balance) and locks the event row (to
  prevent bets on an event that might simultaneously be closing). This prevents race conditions – e.g., two concurrent
  bets subtracting from one balance, or a bet happening at the exact time an outcome is declared.
* **Prefetching & Caching External Data:** The service design favors local caching of external data. A sync process runs
  at startup to import events, drivers, and odds from the external API into the local DB. The API then serves data from
  the database, which improves performance and decouples user requests from the third-party API’s latency or
  availability. It also means the service can survive restarts or external outages with the data it already cached.
* **Flexible Provider Integration:** The external F1 data source is abstracted behind an interface (`F1ExternalApi`),
  and the current implementation (`OpenF1Client`) is selected via configuration. This makes the code ready to accept new
  providers. The database schema even supports multiple providers (with tables for external references keyed by
  provider). This decoupling ensures adding a new data provider (e.g., Ergast API) is straightforward and doesn’t
  require changes to core business logic.
* **Odds Versioning via Database View:** Instead of storing a single mutable odds value, the design stores every odds
  update as a new record (timestamped) in an `event_odds` table. A PostgreSQL **view** (`current_event_odds_view`)
  always selects the latest odds per event-driver. This way, listing events joins on the view to get the current odds
  for each driver. If odds are updated (new records inserted), the next query automatically shows the new odds. This
  approach cleanly separates concerns and would allow implementing real-time odds updates without changing query logic.
* **Thorough Testing & Quality:** The codebase includes both unit tests and integration tests to ensure correctness. The
  external API client is covered by unit tests using MockWebServer (to verify JSON parsing and error handling), and
  business logic is validated with JPA and MockMvc in integration tests. An embedded Postgres is used during integration
  tests to mirror real database behavior, and key scenarios (insufficient funds, duplicate bets, event already closed,
  concurrent bet vs. outcome, etc.) are verified. This gives confidence that edge cases are handled as expected.

## How to Add a New Provider

Adding a new F1 event data provider (beyond the default OpenF1) can be done with minimal changes:

1. **Define the Provider:** Add the new provider name to the `ProviderName` enum (e.g. `ERGAST` if adding the Ergast
   API). Also insert a row for this provider in the `providers` table (so it has a UUID and is recognized).
2. **Implement `F1ExternalApi`:** Create a new client class (e.g. `ErgastClient`) that implements the `F1ExternalApi`
   interface, providing implementations for `listEvents(...)` and `listDrivers(...)` using the new provider’s API. Parse
   the provider’s responses into the provided DTOs (`ExternalEventDto`, `ExternalDriverDto`).
3. **Configure the Bean:** Register the new client as a Spring bean and modify the configuration to include it. In
   `F1ApiConfig`, add a case in the switch for the new provider to return your client bean. Also update application
   properties to set the base URL or any required settings for the new API.
4. **Test & Sync:** Ensure that the new provider’s data format is compatible with the existing sync logic (or adjust the
   mappers accordingly). Once the above is done, you can switch the active provider by changing
   `f1-external-api.active-provider` in configuration, and the system will use the new provider’s API without further
   code changes.
