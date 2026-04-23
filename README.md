#  Smart Campus Sensor & Room Management API

**Module:** 5COSC022W – Client-Server Architectures  
**Student:** Kavindu Subhash  
**ID:** 20232223  

---

##  API Overview

This project is a RESTful API developed using **JAX-RS (Jakarta EE)** and deployed on **Apache Tomcat 9**.

It simulates a Smart Campus system where:

- Rooms can be managed  
- Sensors are assigned to rooms  
- Sensor readings are recorded and retrieved  

The system uses **in-memory storage (ConcurrentHashMap)** and demonstrates:

- RESTful design  
- Sub-resources  
- Query filtering  
- Exception handling  
- Logging using filters  

---

##  Resource Structure


/api/v1
├── /rooms
│ └── /{roomId}
├── /sensors
│ └── /{sensorId}
│ └── /readings


---

##  How to Build & Run

### Prerequisites
- JDK 17 or 21  
- Apache Tomcat 9  
- NetBeans (recommended)  

### Steps (NetBeans)

1. Clone repository:
   ```bash
   git clone https://github.com/<your-username>/smart-campus-api.git
Open project in NetBeans
Configure Tomcat:
Services → Servers → Add Server → Apache Tomcat
Right-click project → Run
Base URL
http://localhost:8080/smart-campus-api/api/v1/
 Sample curl Commands
# 1. API discovery
curl -X GET http://localhost:8080/smart-campus-api/api/v1/

# 2. Create room
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"LIB-301","name":"Library","capacity":50}'

# 3. Create sensor
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"LIB-301"}'

# 4. Filter sensors
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"

# 5. Add reading
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings \
-H "Content-Type: application/json" \
-d '{"value":412.5}'
 Features
RESTful API using JAX-RS
In-memory data storage
Sub-resource for sensor readings
Query parameter filtering
Custom exception handling
Logging filter
 Coursework Questions & Answers
 Part 1
Q1: JAX-RS Lifecycle

JAX-RS uses a per-request lifecycle, meaning a new resource instance is created for each request. To persist data across requests, a shared DataStore with static ConcurrentHashMap is used. This ensures thread-safe data access.

Q2: HATEOAS

HATEOAS allows the API to provide links to related resources. This makes the API self-discoverable and reduces dependency on external documentation.

 Part 2
Q3: IDs vs Full Objects

Returning full objects increases response size but reduces the number of requests. It is more efficient for clients that need complete data.

Q4: DELETE Idempotency

DELETE is idempotent because repeating the request results in the same final state — the resource remains deleted.

 Part 3
Q5: @Consumes JSON

If a client sends an unsupported format, the server returns 415 Unsupported Media Type automatically.

Q6: QueryParam vs Path

Query parameters are better for filtering because they are optional and flexible, e.g. /sensors?type=CO2.

 Part 4
Q7: Sub-resource Locator

Sub-resources improve code organisation by separating logic into different classes, making the system easier to maintain and scale.

 Part 5
Q8: 422 vs 404

422 is used when the request is valid but contains incorrect data. 404 is used when the URL itself is not found.

Q9: Security Risks of Stack Traces

Stack traces expose internal system details. A global exception mapper hides these details and returns safe error messages.

Q10: Logging Filters

Filters centralise logging logic, ensuring consistency and avoiding repetition across resource methods.

 Author

Kavindu Subhash
Student ID: 20232223
