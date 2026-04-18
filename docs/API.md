# API Reference

Complete endpoint documentation for the Smart Stadium System backend API.

**Base URL:** `http://localhost:8080` (local) or your Cloud Run service URL.

## Standardized Error Responses (RFC 7807)

Following the Zero-VPC security hardening, all API errors (e.g., 400 Bad Request, 404 Not Found, 429 Too Many Requests) are returned as standardized `ProblemDetail` JSON objects.

**Example `429 Too Many Requests` (Bucket4j Rate Limit):**
```json
{
  "type": "about:blank",
  "title": "Too Many Requests",
  "status": 429,
  "detail": "API rate limit exceeded. Please try again later.",
  "instance": "/api/route"
}
```

---

## Stadium Zones

### `GET /api/zones`

Returns a dynamic list of all valid stadium zones for frontend UI consumption, decoupling the React client from backend domain logic.

**Response:** `200 OK`
```json
[
  {
    "id": "GATE_A",
    "name": "Gate A"
  },
  {
    "id": "FOOD_COURT_EAST",
    "name": "Food Court East"
  }
]
```

---

## Crowd Density

### `GET /api/crowd-density`

Returns crowd density data for all stadium zones.

**Response:** `200 OK`

```json
[
  {
    "zone": "GATE_A",
    "displayName": "Gate A",
    "currentCount": 245,
    "capacity": 500,
    "occupancyRate": 0.49,
    "densityLevel": "MEDIUM",
    "timestamp": "2026-04-16T10:30:00Z"
  },
  {
    "zone": "FOOD_COURT_EAST",
    "displayName": "Food Court East",
    "currentCount": 270,
    "capacity": 300,
    "occupancyRate": 0.9,
    "densityLevel": "CRITICAL",
    "timestamp": "2026-04-16T10:30:00Z"
  }
]
```

---

### `GET /api/crowd-density/{zone}`

Returns crowd density data for a specific zone.

**Path Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `zone` | string | Yes | Zone identifier (case-insensitive) |

**Valid Zone Values:**
`GATE_A`, `GATE_B`, `GATE_C`, `SEATING_NORTH`, `SEATING_SOUTH`, `FOOD_COURT_EAST`, `FOOD_COURT_WEST`, `RESTROOM_NORTH`, `RESTROOM_SOUTH`, `MAIN_CONCOURSE`, `VIP_LOUNGE`

**Example:** `GET /api/crowd-density/GATE_A`

**Response:** `200 OK`

```json
{
  "zone": "GATE_A",
  "displayName": "Gate A",
  "currentCount": 245,
  "capacity": 500,
  "occupancyRate": 0.49,
  "densityLevel": "MEDIUM",
  "timestamp": "2026-04-16T10:30:00Z"
}
```

**Error:** `400 Bad Request` (invalid zone)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid zone: 'INVALID'. Valid zones: GATE_A, GATE_B, GATE_C, SEATING_NORTH, SEATING_SOUTH, FOOD_COURT_EAST, FOOD_COURT_WEST, RESTROOM_NORTH, RESTROOM_SOUTH, MAIN_CONCOURSE, VIP_LOUNGE",
  "path": "/api/crowd-density/INVALID",
  "timestamp": "2026-04-16T10:30:00Z"
}
```

---

## Unified Telemetry

### `GET /api/telemetry`

Returns the complete stadium state (crowd densities and queue wait times) in a single optimized payload. **This is the preferred endpoint for initial dashboard loading.**

**Response:** `200 OK`

```json
{
  "crowdDensities": [
    {
      "zone": "GATE_A",
      "displayName": "Gate A",
      "currentCount": 245,
      "capacity": 500,
      "occupancyRate": 0.49,
      "densityLevel": "MEDIUM",
      "timestamp": "2026-04-16T10:30:00Z"
    }
  ],
  "queueWaitTimes": [
    {
      "zone": "FOOD_COURT_EAST",
      "displayName": "Food Court East",
      "queueLength": 18,
      "estimatedWaitSeconds": 810,
      "densityLevel": "MEDIUM",
      "timestamp": "2026-04-16T10:30:00Z"
    }
  ]
}
```

---

## Route Calculation

### `GET /api/route`

Finds the optimal route between two zones using the **A* (A-Star)** algorithm with Euclidean distance heuristics and crowd-density-adjusted weights.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `from` | string | Yes | Starting zone identifier |
| `to` | string | Yes | Destination zone identifier |

**Example:** `GET /api/route?from=GATE_A&to=FOOD_COURT_EAST`

**Response:** `200 OK`

```json
{
  "from": "GATE_A",
  "to": "FOOD_COURT_EAST",
  "path": ["GATE_A", "MAIN_CONCOURSE", "FOOD_COURT_EAST"],
  "pathDisplayNames": ["Gate A", "Main Concourse", "Food Court East"],
  "estimatedTimeSeconds": 109,
  "totalWeight": 130.0
}
```

**Error:** `400 Bad Request` (missing parameter)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Missing required parameter: from",
  "path": "/api/route",
  "timestamp": "2026-04-16T10:30:00Z"
}
```

---

## Queue Wait Time

### `GET /api/wait-time`

Returns estimated wait times for all zones.

**Response:** `200 OK`

```json
[
  {
    "zone": "FOOD_COURT_EAST",
    "displayName": "Food Court East",
    "queueLength": 18,
    "estimatedWaitSeconds": 810,
    "densityLevel": "MEDIUM",
    "timestamp": "2026-04-16T10:30:00Z"
  },
  {
    "zone": "RESTROOM_NORTH",
    "displayName": "Restroom North",
    "queueLength": 7,
    "estimatedWaitSeconds": 630,
    "densityLevel": "LOW",
    "timestamp": "2026-04-16T10:30:00Z"
  }
]
```

---

### `GET /api/wait-time?zone=X`

Returns the estimated wait time for a specific zone.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `zone` | string | Yes | Zone identifier (case-insensitive) |

**Example:** `GET /api/wait-time?zone=FOOD_COURT_EAST`

**Response:** `200 OK`

```json
{
  "zone": "FOOD_COURT_EAST",
  "displayName": "Food Court East",
  "queueLength": 18,
  "estimatedWaitSeconds": 810,
  "densityLevel": "MEDIUM",
  "timestamp": "2026-04-16T10:30:00Z"
}
```

---

## Health Check

### `GET /actuator/health`

Application health check (Spring Boot Actuator).

**Response:** `200 OK`

```json
{
  "status": "UP"
}
```

---

## Response Fields Reference

### Density Levels

| Level | Occupancy Range | Description |
|---|---|---|
| `LOW` | 0% – 29% | Comfortable, low traffic |
| `MEDIUM` | 30% – 59% | Moderate traffic |
| `HIGH` | 60% – 84% | Heavy traffic, may experience delays |
| `CRITICAL` | 85% – 100% | At or near capacity, expect congestion |

### Error Response Format

All errors follow a consistent structure:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable error description",
  "path": "/api/endpoint",
  "timestamp": "2026-04-16T10:30:00Z"
}
```

- No stack traces are included in error responses
- Internal server errors return a generic message
