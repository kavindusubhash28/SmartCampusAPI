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
   git clone https: https://github.com/kavindusubhash28/SmartCampusAPI.git)
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

**Q1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.** 

Resource classes in JAX-RS utilize request-per-instance lifecycle, which means that for every HTTP request a new object is created. As a result, no instance information will be available to multiple users and provides thread-safety of the application. In such way, any information kept within resource objects does not survive beyond the point of completion of the corresponding HTTP request. Hence, resource objects cannot be treated as primary storage for the application's data. Instead, a common in-memory collection DataStore, with the help of static collections, can be used. Being processed by multiple concurrent threads, thread-safety becomes an important issue.

---

**Q2: Why is the provision of ”Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?**  

The principle HATEOAS stands for Hypermedia as the Engine of Application State, which indicates that an API response must contain links that point to some other relevant resources or actions. Thus, an API becomes self-descriptive as a client is able to discover resources or actions using links rather than hardcoded URLs. The principle of HATEOAS lowers coupling between a client and a server, because a client does not require any previous information about all endpoints. It will work even in case when a server changes its URL structure; clients will still be able to perform their operations following links returned by a server.

---

###  Part 2

**Q3: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing.**  

The fact that the response will contain only the IDs will result in a smaller response and lower use of the bandwidth, which is an advantage as far as performance goes. But this means that further requests must be made by the client in order to obtain the details of each object. In this way, the "N+1 request problem" is introduced, where N corresponds to the number of items. The other solution is to return the entire object, which may result in a larger response.

---

**Q4: Is the DELETE operation idempotent in your implementation? Provide a detailed
justification by describing what happens if a client mistakenly sends the exact same DELETE
request for a room multiple times.** 

DELETE is idempotent. In the case of idempotence, issuing the same request more than once should end with the same result as if issued once. When there is a room which gets deleted on the first issue of DELETE, then sending another DELETE request will not have an effect since the room is no longer there. So, the system’s state is “Room doesn’t exist anymore.”

But note that the response message could be different from one request to another. It is because it can happen that the first DELETE resulted to 200 OK response whereas the second will return 404 NOT FOUND. But remember that what idempotence talks about is the server’s final state rather than the response message.

---

###  Part 3

**Q5: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?**  

"@Consumes(MediaType.APPLICATION_JSON)" annotation applied to the method means that the method consumes only JSON data as a part of the request body. The content-type specified in the Content-Type header gets validated before calling the method by the JAX-RS implementation to make sure that this method is able to consume data contained in the request body.

HTTP Status 415 "Unsupported Media Type" will be returned in almost all cases. It is convenient since it allows you to make sure that an API is consistent when consuming requests without any additional validation of incoming data in methods of each resource class.

---

**Q6: You implemented this filtering using @QueryParam. Contrast this with an alternative
design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the query parameter approach generally considered superior for filtering and searching
collections?**  

The use of @QueryParam is a preferred way to filter resources since it changes the view of a collection instead of the resource itself. An endpoint like /api/v1/sensors?type=CO2 makes it clear that this is a filtering process. However, endpoints like /api/v1/sensors/type/CO2 could be confused with a sub-resource of the sensors collection. Another reason why query parameters should be used for filtering is that they are optional and flexible. For instance, it is easy to apply multiple filters like /sensors?type=CO2&status=ACTIVE.

---

###  Part 4

**Q7: Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller
class?Sub-resource Locator** 

Sub-Resource Locator is an example of an approach that makes APIs better by allowing the treatment of nested resources in a different class. Instead of trying to place all functionality in one class for every resource, the Sub-Resource Locator makes it possible to delegate handling of a deeper path to different classes. Thus, each class has only one purpose to deal with. Another advantage of the approach is that it makes the code much more readable and maintainable as well.

---

###  Part 5

**Q8: Why is HTTP 422 often considered more semantically accurate than a standard
404 when the issue is a missing reference inside a valid JSON payload?**  

HTTP 404 Not Found implies that the URL or resource path provided by the client does not exist. In the current case, although the client requests POST /api/v1/sensors, the URL is perfectly valid. This means the issue does not stem from the endpoint; the issue lies in the presence of a roomId in the JSON body, which does not refer to an existing room.

This explains why 422 Unprocessable Entity would be more appropriate. In this case, the server correctly understands the format of the request made by the client and its syntax. Nevertheless, the server cannot complete processing the request due to semantic errors in the data contained in it. In essence, the data in the JSON object is well-formatted, but the entity being referred to does not exist.

---

**Q9: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?** 

Providing clients with the stack trace from a Java application is a security threat because it exposes the internal workings of the application. This can expose sensitive information such as the names of classes, files, and methods as well as any frameworks used. All this information helps the attacker gain insight into how the application is structured. They will then use this information to exploit weaknesses or even formulate more advanced attacks, especially if they know the version of any frameworks used.

---

**Q10: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single resource
method?**  

Using JAX-RS filters for logging is better because logging is a cross-cutting concern applied across all endpoints. Writing logging code in each method would cause duplication and make the code harder to maintain. Filters centralise logging in one place, making the system cleaner and more organised. A request filter can log incoming requests, while a response filter logs outgoing responses. This ensures consistent logging across the API. It also keeps resource methods focused on business logic. Overall, filters improve maintainability and reduce code repetition.
