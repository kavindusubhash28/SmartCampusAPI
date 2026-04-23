#  Smart Campus Sensor & Room Management API

##  Module
**5COSC022W – Client-Server Architectures**

---

##  Project Description

This project is a RESTful API developed using **Jakarta EE (JAX-RS)** and deployed on **Apache Tomcat**.

The API simulates a Smart Campus system where:

- Rooms can be created, retrieved, and deleted  
- Sensors can be assigned to rooms  
- Sensor readings can be recorded and accessed  
- Relationships between resources are maintained  

The system uses **in-memory storage (HashMap)** and demonstrates core REST concepts including:

- Resource-based design  
- Sub-resources  
- Filtering with query parameters  
- Exception handling  
- Logging using filters  

---

##  Technologies Used

- Java (JDK 17 / 21)  
- Jakarta EE (JAX-RS)  
- Apache Tomcat 9  
- Maven  
- NetBeans IDE  
- Postman (for testing)  

---

##  How to Run the Project

### Prerequisites
- JDK installed  
- Apache Tomcat configured in NetBeans  
- NetBeans IDE  

### Steps

1. Open the project in NetBeans  
2. Ensure Apache Tomcat is added (**Services → Servers**)  
3. Right-click project → **Run**

### API Base URL


http://localhost:8080/smart-campus-api/api/v1/


---

##  API Endpoints

###  Discovery

GET /api/v1/


###  Rooms

GET /api/v1/rooms
POST /api/v1/rooms
GET /api/v1/rooms/{roomId}
DELETE /api/v1/rooms/{roomId}


###  Sensors

GET /api/v1/sensors
POST /api/v1/sensors
GET /api/v1/sensors?type={type}


###  Sensor Readings (Sub-resource)

GET /api/v1/sensors/{sensorId}/readings
POST /api/v1/sensors/{sensorId}/readings


---

##  Testing Instructions

Testing was performed using **Postman**.

### Example Tests

- Create Room → `POST /rooms`  
- Get Rooms → `GET /rooms`  
- Create Sensor → `POST /sensors`  
- Filter Sensors → `GET /sensors?type=Temperature`  
- Add Reading → `POST /sensors/{id}/readings`  
- Get Readings → `GET /sensors/{id}/readings`  

---

###  Error Handling Tests

- Delete room with sensors → **409 Conflict**  
- Create sensor with invalid room → **422 Unprocessable Entity**  
- Add reading to maintenance sensor → **403 Forbidden**  

---

###  Logging

All requests and responses are logged using a **JAX-RS filter**, including:

- HTTP method  
- URL  
- Response status  

---

##  Features Implemented

- RESTful API using JAX-RS  
- In-memory data storage using HashMap  
- Sub-resource locator pattern  
- Query parameter filtering  
- Custom exception handling  
- Exception mappers for HTTP responses  
- Logging filter for request tracking  

---

#  Coursework Questions & Answers

---

##  Part 1

### Q1: JAX-RS Lifecycle

By default, JAX-RS creates a new instance of a resource class for each incoming request (per-request lifecycle). This ensures that instance variables are not shared between threads, avoiding concurrency issues. However, it also means that data stored inside resource classes is not persistent across requests. To solve this, a separate `DataStore` class with static maps is used, ensuring data persists and is shared safely across requests.

---

### Q2: HATEOAS

HATEOAS allows API responses to include links to related resources, making the API self-discoverable. In this project, the discovery endpoint provides links to `/rooms` and `/sensors`. This reduces dependency on external documentation and allows clients to dynamically navigate the API even if endpoints change.

---

##  Part 2

### Q3: IDs vs Full Objects

Returning only IDs reduces response size and bandwidth usage but requires additional requests to fetch full data. Returning full objects increases payload size but provides all necessary information in one request, improving client efficiency. In this system, full objects are preferred for usability.

---

### Q4: DELETE Idempotency

DELETE is idempotent because repeated requests produce the same final state. The first DELETE removes the resource, and subsequent requests return 404, but the resource remains deleted. Therefore, the system state does not change after the first request.

---

##  Part 3

### Q5: @Consumes JSON

If a client sends data in a format other than JSON (e.g., text/plain), JAX-RS automatically returns **415 Unsupported Media Type**. The request is rejected before reaching the resource method, ensuring only valid formats are processed.

---

### Q6: QueryParam vs Path

Query parameters are ideal for filtering because they are optional and do not change the identity of the resource. Using `/sensors?type=CO2` is cleaner and more flexible than embedding filters in the path. It also allows easy extension for multiple filters.

---

##  Part 4

### Q7: Sub-resource Locator

The sub-resource locator pattern separates logic into different classes. Instead of handling everything in one large class, `SensorResource` delegates reading-related operations to `SensorReadingResource`. This improves code organization, readability, and scalability.

---

##  Part 5

### Q8: 422 vs 404

HTTP 422 is more appropriate when the request is syntactically correct but contains invalid data (e.g., referencing a non-existent room). A 404 indicates a missing URL, not invalid request content.

---

### Q9: Security Risks of Stack Traces

Exposing stack traces can reveal internal structure, class names, file paths, and library versions. This information can be used by attackers to exploit vulnerabilities. Therefore, a `GlobalExceptionMapper` is used to return safe error messages while logging details internally.

---

### Q10: Logging Filters

Using a logging filter centralizes logging logic and avoids repetition across resource methods. It ensures consistent logging for all requests and simplifies maintenance compared to manually adding logging statements in each method.

---

##  Author

**Kavindu Subhash**  
Student ID: 20232223 

---

##  Final Notes

- This project follows RESTful design principles  
- All endpoints are tested and functional  
- The API is deployed using Apache Tomcat  
