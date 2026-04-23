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

## 📘 Coursework Questions & Answers

---

###  Part 1

**Q1: JAX-RS Lifecycle**  
JAX-RS uses a per-request lifecycle, meaning a new resource instance is created for each request. This prevents shared state between requests and improves thread safety. However, data stored inside resource classes does not persist, so a shared `DataStore` using static `ConcurrentHashMap` is used to maintain data safely across multiple requests.

---

**Q2: HATEOAS**  
HATEOAS allows API responses to include links to related resources, making the API self-discoverable. This reduces dependency on hardcoded URLs and documentation, and allows clients to navigate the API dynamically even if endpoints change.

---

###  Part 2

**Q3: IDs vs Full Objects**  
Returning only IDs reduces response size but requires additional requests to fetch full details. Returning full objects increases response size but provides complete information in a single request. This improves client efficiency, especially when full data is needed immediately.

---

**Q4: DELETE Idempotency**  
DELETE is idempotent because repeating the same request results in the same final state. Once a resource is deleted, further DELETE requests do not change the system, even if the response changes (e.g., 404).

---

###  Part 3

**Q5: @Consumes JSON**  
The `@Consumes` annotation ensures the API only accepts JSON input. If a client sends a different format, JAX-RS automatically returns **415 Unsupported Media Type**, preventing invalid data processing.

---

**Q6: QueryParam vs Path**  
Query parameters are better for filtering because they are optional and flexible. For example, `/sensors?type=CO2` filters results without changing the resource path. They are also easier to extend with multiple filters.

---

###  Part 4

**Q7: Sub-resource Locator**  
The sub-resource locator pattern separates nested resource logic into different classes. This improves code organisation, readability, and makes the system easier to maintain and scale.

---

###  Part 5

**Q8: 422 vs 404**  
404 means the requested URL does not exist, while 422 means the request is valid but contains incorrect data. In this case, 422 is more accurate because the error is in the request body, not the endpoint.

---

**Q9: Security Risks of Stack Traces**  
Stack traces expose internal details such as class names and file paths, which can help attackers. To avoid this, a global exception mapper returns safe error messages while logging details internally.

---

**Q10: Logging Filters**  
Logging filters centralise logging for all requests and responses. This avoids repeating logging code in every method and ensures consistent and maintainable logging across the application.
