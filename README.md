# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W – Client-Server Architectures  
**Student:** Kavindu Subhash | **ID:** 20232223  

---

## API Design Overview

This API is a RESTful web service built with **JAX-RS (Jakarta EE)** and deployed on **Apache Tomcat 9**. It manages campus Rooms and IoT Sensors using in-memory `ConcurrentHashMap` storage — no database is used.

### Resource Hierarchy

```
/api/v1                          ← Discovery / HATEOAS root
├── /rooms                       ← Room collection
│   └── /{roomId}               ← Individual room resource
├── /sensors                     ← Sensor collection
│   └── /{sensorId}            ← Individual sensor resource
│       └── /readings           ← Sub-resource: historical readings
```

### Design Decisions

- **Versioned base path** `/api/v1` set via `@ApplicationPath`
- **Resource-based URLs** — nouns not verbs, following REST conventions
- **Standard HTTP verbs** — GET, POST, DELETE used semantically correct
- **HATEOAS** — discovery endpoint returns navigable links to all collections
- **Sub-resource locator** — readings logic delegated to a dedicated `SensorReadingResource` class
- **Structured error responses** — every error returns a JSON body with `status`, `error`, `message`, and `timestamp`; raw stack traces are never exposed
- **Thread-safe in-memory storage** — `static ConcurrentHashMap` fields in a `DataStore` singleton, safely shared across per-request resource instances

### Data Models

**Room**
```json
{ "id": "LIB-301", "name": "Library Quiet Study", "capacity": 50, "sensorIds": ["CO2-001"] }
```
**Sensor**
```json
{ "id": "CO2-001", "type": "CO2", "status": "ACTIVE", "currentValue": 412.5, "roomId": "LIB-301" }
```
**SensorReading**
```json
{ "id": "a3f1c2d4-uuid", "timestamp": 1714046400000, "value": 412.5 }
```
**Error Response**
```json
{ "status": 409, "error": "Conflict", "message": "Room has sensors assigned.", "timestamp": 1714046400000 }
```

### Endpoints Summary

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/api/v1/` | API discovery with HATEOAS links | 200 |
| GET | `/api/v1/rooms` | List all rooms | 200 |
| POST | `/api/v1/rooms` | Create a new room | 201 |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (blocked if sensors assigned) | 200 / 409 |
| GET | `/api/v1/sensors` | List all sensors (optional `?type=` filter) | 200 |
| POST | `/api/v1/sensors` | Register a sensor (validates roomId exists) | 201 / 422 |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor | 200 |
| DELETE | `/api/v1/sensors/{sensorId}` | Remove a sensor | 200 |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Record a new reading (blocked if MAINTENANCE) | 201 / 403 |

---

## How to Build and Run

### Prerequisites

- JDK 17 or 21 installed
- Apache Tomcat 9 installed
- NetBeans IDE (recommended) or any Maven-capable IDE
- Maven 3.x installed

### Option A — Run via NetBeans (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/smart-campus-api.git
   ```

2. Open **NetBeans IDE** → `File` → `Open Project` → select the `smart-campus-api` folder

3. Confirm Tomcat 9 is configured:
   - Go to `Services` → `Servers`
   - If Tomcat is not listed: click `Add Server` → select `Apache Tomcat` → point to your Tomcat installation directory → finish wizard

4. Right-click the project in the Projects panel → click **Run**

5. NetBeans will compile, deploy to Tomcat, and start the server. The API is live at:
   ```
   http://localhost:8080/smart-campus-api/api/v1/
   ```

### Option B — Maven Command Line

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/smart-campus-api.git
   cd smart-campus-api
   ```

2. Build the WAR file:
   ```bash
   mvn clean package
   ```
   Output: `target/smart-campus-api.war`

3. Copy the WAR into Tomcat's webapps directory:
   ```bash
   cp target/smart-campus-api.war /path/to/tomcat/webapps/
   ```

4. Start Tomcat:
   ```bash
   # macOS / Linux
   /path/to/tomcat/bin/startup.sh

   # Windows
   /path/to/tomcat/bin/startup.bat
   ```

5. The API is live at:
   ```
   http://localhost:8080/smart-campus-api/api/v1/
   ```

### Verify the Server is Running

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/
```

Expected output:
```json
{
  "apiName": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "_links": { "rooms": "/api/v1/rooms", "sensors": "/api/v1/sensors" }
}
```

---

## Sample curl Commands

> All commands use base URL `http://localhost:8080/smart-campus-api`. Adjust if your port or context path differs.

### 1. API Discovery
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/ \
  -H "Accept: application/json"
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'
```

### 3. Create a Sensor and Assign to a Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "CO2-001", "type": "CO2", "status": "ACTIVE", "currentValue": 0.0, "roomId": "LIB-301"}'
```

### 4. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```

### 5. Post a Sensor Reading (also updates sensor currentValue)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 412.5}'
```

### 6. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings \
  -H "Accept: application/json"
```

### 7. Get a Specific Room
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

---

## Report — Answers to Coursework Questions

---

### Part 1 — Q1: JAX-RS Resource Class Lifecycle

By default, JAX-RS follows a **per-request lifecycle**: the runtime creates a brand-new instance of each resource class for every incoming HTTP request, and discards that instance once the response is sent. This design exists for thread safety — because each request receives its own isolated object, instance fields are never shared between concurrent requests, eliminating an entire category of concurrency bugs.

However, this behaviour creates a critical problem for in-memory storage: any data held in instance fields of a resource class is lost the moment the request ends. A sensor created during one POST would not exist when the next GET arrived, because that request is served by a completely different object instance.

To solve this, the project uses a dedicated **`DataStore`** class with **`static ConcurrentHashMap` fields**. Because `static` fields belong to the class loader rather than any object instance, they persist across all requests for the entire lifetime of the deployed application and are shared across every resource instance. `ConcurrentHashMap` is chosen over a plain `HashMap` because Tomcat processes multiple requests on multiple threads simultaneously. `ConcurrentHashMap` provides thread-safe reads and writes internally without requiring manual `synchronized` blocks, preventing data corruption or race conditions when two requests attempt to write to the store at the same moment.

---

### Part 1 — Q2: HATEOAS and Its Benefits

**HATEOAS** (Hypermedia as the Engine of Application State) is the principle that API responses should include hyperlinks to related or next-step resources within the response body, rather than only returning raw data. It represents Level 3 in the Richardson Maturity Model — the highest level of REST compliance — because it transforms the API from a simple data-fetching interface into a self-describing, fully navigable system.

The primary benefit over static documentation is **runtime discoverability**. A client that calls the root discovery endpoint (`GET /api/v1`) receives the URLs of every primary resource collection directly in the response (`/api/v1/rooms`, `/api/v1/sensors`). The client can navigate the entire API by following these links without needing to hard-code any paths or consult external documentation.

A second major benefit is **resilience to change**. If a server-side URL is restructured, a HATEOAS-compliant client automatically picks up the updated link from the next API response rather than breaking silently. Static documentation becomes outdated the moment the API changes, meaning developers must manually identify and update every hard-coded URL in client applications. HATEOAS eliminates this maintenance burden by keeping navigation information in the API itself.

---

### Part 2 — Q3: IDs vs Full Objects When Listing Rooms

**Returning only IDs** produces a small response payload, reducing bandwidth for the initial list request. However, it forces the client to make a separate HTTP request for every room it needs full details on. This is the "N+1 request problem" — for a campus with hundreds of rooms, this means hundreds of sequential API calls, causing significant cumulative latency and a poor user experience.

**Returning full objects** increases the size of a single response but delivers everything the client needs in one network round-trip. For the expected primary use case of this API — a facilities dashboard that renders all room data — this is far more efficient. The trade-off is higher bandwidth per request, but this is preferable in practice to multiple sequential requests. A production-grade API could mitigate the bandwidth cost by supporting sparse fieldsets via a `?fields=id,name` query parameter, allowing clients to request only the fields they need. In this implementation, full objects are returned as the most practical and client-friendly default.

---

### Part 2 — Q4: Is DELETE Idempotent?

Yes, DELETE is **idempotent** in this implementation, fully consistent with RFC 7231.

Idempotency means that making the same request multiple times produces the **same server state** as making it once. In this system:

- **First DELETE request:** The room exists, the server removes it from `DataStore`, returns `200 OK`.
- **Second and subsequent DELETE requests:** The room no longer exists, the server returns `404 Not Found`.

Although the HTTP **status code** differs between the first and subsequent calls, the **state of the server** is identical after each — the room does not exist in either case. RFC 7231 explicitly states that idempotency refers to server-side state effects, not response codes. The resource remains absent regardless of how many times the DELETE is repeated, satisfying the definition completely. A correctly implemented client should handle a `404` on a repeated DELETE gracefully as an expected and acceptable outcome, not as an unexpected error.

---

### Part 3 — Q5: @Consumes and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares that the POST endpoint only accepts requests with a `Content-Type: application/json` header. When a client sends data with any other `Content-Type` — such as `text/plain` or `application/xml` — the JAX-RS runtime intercepts the request **before the resource method body is ever executed**. It inspects the `Content-Type` header, finds no matching `@Consumes` declaration for that media type, and automatically returns an **HTTP 415 Unsupported Media Type** response.

This is JAX-RS's built-in **content negotiation** mechanism handling format validation at the infrastructure layer, meaning no manual type-checking code is needed inside the method itself. The same mechanism operates in reverse via `@Produces` and the client's `Accept` header: if a client requests `Accept: application/xml` but the method declares only `@Produces(MediaType.APPLICATION_JSON)`, JAX-RS automatically returns `406 Not Acceptable` before the method runs.

---

### Part 3 — Q6: @QueryParam vs Path-Based Filtering

The URL path in REST architecture identifies a **resource** — a stable, meaningful entity or collection. The path `/api/v1/sensors` correctly identifies the sensor collection as a resource. A design like `/api/v1/sensors/type/CO2` falsely implies that `type/CO2` is a distinct sub-resource, suggesting it is a separate entity rather than a filtered view of the same collection. This is semantically misleading and produces a rigid URL structure.

**Query parameters** are designed precisely for filtering, searching, sorting, and pagination — operations that modify *how* a collection is returned, not *which resource* is being targeted. `GET /api/v1/sensors?type=CO2` correctly reads as "return the sensor collection, filtered to type CO2."

Specific advantages of `@QueryParam` over path-based filtering:

1. **Optionality by design** — `GET /api/v1/sensors` (no filter) and `GET /api/v1/sensors?type=CO2` (filtered) both resolve to the same endpoint without extra routes.
2. **Natural composability** — multiple filters combine cleanly: `?type=CO2&status=ACTIVE`. Embedding filters in paths produces an explosion of URL patterns to cover every combination.
3. **HTTP tooling compatibility** — caches, API gateways, and proxies correctly treat query strings as modifiers of a base resource URL and handle them accordingly.

---

### Part 4 — Q7: Sub-Resource Locator Pattern Benefits

The Sub-Resource Locator pattern allows a resource class to delegate handling of a URL sub-path to a separate dedicated class, rather than defining every nested route inside one monolithic class. In this project, `SensorResource` handles `/api/v1/sensors` and delegates `/api/v1/sensors/{sensorId}/readings` to a separate `SensorReadingResource` class by returning an instance of it from a locator method annotated only with `@Path`.

**Separation of Concerns:** Each class has one clearly defined responsibility. `SensorResource` manages sensor CRUD operations. `SensorReadingResource` manages the readings lifecycle for a given sensor. Neither class needs to know about the internal implementation of the other.

**Maintainability at Scale:** In a large real-world API, defining every nested path in a single controller class would produce thousands of lines that are extremely difficult to navigate and maintain. Sub-resource locators allow the codebase to grow horizontally by adding small, focused resource classes rather than endlessly expanding existing ones.

**Context Propagation:** The sensor ID is passed to `SensorReadingResource` via its constructor when the locator method executes. The sub-resource class always has the correct context without re-extracting it from the URL inside every method, reducing repetition and the chance of bugs.

**Testability:** Small, focused classes are significantly easier to unit test in isolation compared to a monolithic class with dozens of methods covering multiple resource domains.

---

### Part 5 — Q8: Why HTTP 422 is More Accurate Than 404

HTTP **404 Not Found** has a precise meaning: the resource identified by the **request URL** does not exist on the server. When a client sends `POST /api/v1/sensors`, the URL is perfectly valid — the endpoint exists and the server successfully received the request. The problem is not a missing URL; it is that a field **inside the JSON request body** (`roomId`) references an entity that does not exist in the system.

HTTP **422 Unprocessable Entity** was designed exactly for this scenario: the server understands the request format (syntactically valid JSON, correct `Content-Type`), but cannot process it because the **semantic content** is logically invalid. A missing foreign-key reference is a semantic validation failure — the payload is well-formed but unprocessable as supplied.

Using 404 would mislead the client into thinking the endpoint URL itself was wrong, sending developers down the wrong debugging path. Using 422 precisely communicates: "your request arrived at the correct endpoint, but the data inside it references something that does not exist." This specificity is essential for API consumers to implement correct and targeted error-handling logic.

---

### Part 5 — Q9: Security Risks of Exposing Java Stack Traces

Exposing raw Java stack traces to external API consumers constitutes a serious **information disclosure vulnerability** for multiple reasons:

**Internal Architecture Mapping:** Stack traces reveal fully qualified class names (e.g. `com.smartcampus.resources.SensorResource`), method names, and line numbers. This gives an attacker a precise map of the application's internal structure, significantly reducing the effort needed to craft targeted exploits.

**Framework and Library Version Fingerprinting:** Traces include names and version numbers of third-party libraries (e.g. Jersey, Jackson, Grizzly). An attacker cross-references these against public CVE databases to identify known, unpatched vulnerabilities for those exact versions.

**Server File Path Leakage:** Stack traces frequently include absolute server-side file paths (e.g. `/opt/tomcat/webapps/smart-campus-api/WEB-INF/classes/...`), revealing the server's directory structure and enabling path traversal attacks.

**Database Schema Exposure:** If a database were in use, SQL exceptions within stack traces would expose table names, column names, and full query structures — directly enabling tailored SQL injection attacks.

The `GlobalExceptionMapper` mitigates all of these risks by catching every unhandled `Throwable`, logging the complete trace **server-side only** where only administrators can see it, and returning a completely generic `500 Internal Server Error` JSON body to the client containing no internal implementation details whatsoever.

---

### Part 5 — Q10: JAX-RS Filters vs Manual Logging

A **cross-cutting concern** is functionality required across many components of an application that is not part of any single component's core business logic — logging, authentication, and CORS headers are classic examples. Handling cross-cutting concerns inside individual resource methods violates two core software engineering principles: **Single Responsibility** (each method should have one purpose) and **DRY — Don't Repeat Yourself** (logic should not be duplicated).

Using a JAX-RS filter implementing both `ContainerRequestFilter` and `ContainerResponseFilter` provides the following concrete advantages over manual logging:

**Centralisation:** All logging logic lives in one class (`ApiLoggingFilter`). There is a single place to update the log format, switch logging frameworks, or add new metadata fields — rather than modifying every resource method individually.

**Guaranteed Completeness:** Because the filter is registered at the framework level and intercepts every request and response automatically, it is impossible to accidentally omit logging from a new endpoint. Manual `Logger.info()` insertion is inherently error-prone, especially as the API grows and multiple developers add endpoints independently.

**Clean Resource Classes:** Resource methods contain only business logic, making them shorter, more readable, and easier to unit test in isolation. The concerns of "what does this endpoint do" and "log that this endpoint was called" are fully and cleanly separated.

**Consistent Output Format:** Every request and response is logged in exactly the same structured format, making logs reliably parseable by monitoring and observability tools — something ad-hoc logging statements scattered across methods written by multiple developers cannot guarantee.
