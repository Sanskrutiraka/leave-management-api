# Employee Leave Management REST API

REST API for managing employee leave requests, built with Spring Boot 3, Java 17, Spring Security (JWT), and Spring Data JPA.

There is no frontend, and the API is tested with the Postman collection.

---

## Prerequisites

- Java 17
- Maven 3.9+
- Docker or PostgreSQL (optional, for the `postgres` profile)

---

## Tech Stack

- **Java:** 17
- **Framework:** Spring Boot 3.2.5 (Spring MVC, Spring Data JPA, Spring Security, Bean Validation)
- **Security:** Stateless JWT (JJWT 0.12.5, HMAC-SHA512)
- **Databases:**
  - H2 in-memory (default)
  - PostgreSQL (via `postgres` profile)
- **Build Tool:** Maven 3.9+

The database passwords and JWT secret in the properties files are development defaults, to be replaced in a real deployment.

---

## Sample Accounts

`DataInitializer` seeds the sample data whenever the database is empty (H2 or Postgres); `schema.sql` is the PostgreSQL schema plus the same sample rows. The app also creates tables automatically when it starts.

| Role | Name | Email | Password | Department |
| :--- | :--- | :--- | :--- | :--- |
| **MANAGER** | Priya Sharma | `priya.sharma@company.com` | `password123` | Engineering (ID: 1) |
| **EMPLOYEE** | Rahul Verma | `rahul.verma@company.com` | `password123` | Engineering (ID: 1) |
| **EMPLOYEE** | Anita Desai | `anita.desai@company.com` | `password123` | Human Resources (ID: 2) |

---

## Running the Application

### Option 1: H2 In-Memory (Default)

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```
   The application runs on `http://localhost:8080`.

2. Access the H2 web console:
   - **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
   - **JDBC URL:** `jdbc:h2:mem:leavedb`
   - **Username:** `sa`
   - **Password:** *(empty)*

### Option 2: PostgreSQL

1. Start PostgreSQL on `localhost:5432`. If using Docker:
   ```bash
   docker run -d --name local-leavedb -e POSTGRES_DB=leavedb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:15-alpine
   ```

2. (Optional) Run `schema.sql` manually:
   - **psql:**
     ```bash
     psql -U postgres -d leavedb -f schema.sql
     ```
   - **Docker (PowerShell):**
     ```powershell
     Get-Content schema.sql | docker exec -i local-leavedb psql -U postgres -d leavedb
     ```
   - **Docker (Bash):**
     ```bash
     docker exec -i local-leavedb psql -U postgres -d leavedb < schema.sql
     ```

3. Start the application with the `postgres` profile:
   ```bash
   mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
   ```
   *(On Linux/macOS: `mvn spring-boot:run -Dspring-boot.run.profiles=postgres`)*

---

## API Endpoints

All endpoints except `/api/auth/**` and `/h2-console/**` require a Bearer token:
```text
Authorization: Bearer <token>
```

### Authentication (`/api/auth`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Public | Authenticates credentials and returns JWT + user info |
| `POST` | `/api/auth/register` | Public | Registers a new employee (`ROLE_EMPLOYEE`) |

### Employee Leave Operations (`/api/leaves`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/leaves` | Authenticated | Create a leave request (status: `PENDING`) |
| `GET` | `/api/leaves/my` | Authenticated | View own leave requests |
| `GET` | `/api/leaves/{id}` | Owner / Dept Mgr | View specific leave details |
| `PUT` | `/api/leaves/{id}` | Owner | Update leave request (only while `PENDING`) |
| `DELETE` | `/api/leaves/{id}` | Owner | Cancel leave request (only while `PENDING`) |

### Sample Leave Request (`POST /api/leaves`, `PUT /api/leaves/{id}`)

```json
{
  "type": "ANNUAL",
  "startDate": "2026-11-01",
  "endDate": "2026-11-05",
  "reason": "Family vacation"
}
```

- `type`: `ANNUAL`, `CASUAL`, `SICK`, or `UNPAID`
- `startDate` / `endDate`: `YYYY-MM-DD` format
- `reason`: required (an empty reason returns HTTP 400)

The curl command below is for Bash. On Windows, use the Postman collection or curl.exe with the body in a file:

```bash
curl -X POST http://localhost:8080/api/leaves \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ANNUAL",
    "startDate": "2026-11-01",
    "endDate": "2026-11-05",
    "reason": "Family vacation"
  }'
```

### Manager Operations (`/api/leaves`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/leaves/department` | `ROLE_MANAGER` | View all leave requests in manager's department |
| `PATCH` | `/api/leaves/{id}/approve` | `ROLE_MANAGER` | Approve a pending leave in manager's department |
| `PATCH` | `/api/leaves/{id}/reject` | `ROLE_MANAGER` | Reject a pending leave in manager's department |

---

## Business Rules & Validation

1. **Date Validation:** `endDate` must not be before `startDate` (HTTP 400).
2. **Overlap Detection:** An employee cannot have overlapping leave requests for the same date range (HTTP 400). Only `APPROVED` or `PENDING` leaves count as overlapping (`REJECTED` leaves do not block dates).
3. **Modification Restrictions:** An employee can only update or cancel a leave request while it is still in `PENDING` status. Modifying or cancelling an `APPROVED` or `REJECTED` leave returns HTTP 400.
4. **Ownership:** Employees can only view, update, or cancel their own leave requests. Attempting to modify another user's request returns HTTP 403.
5. **Department Scope:** Managers can only view, approve, or reject leave requests submitted by employees within their own department. Approving or rejecting requests from another department returns HTTP 403.
6. **No Self-Approval:** A manager cannot approve or reject their own leave requests (HTTP 403).
7. **Error Responses:** Errors return a JSON payload:
   ```json
   {
     "timestamp": "2026-10-15T22:29:34.331",
     "status": 400,
     "error": "Bad Request",
     "message": "Employee already has an overlapping leave request for these dates",
     "path": "/api/leaves"
   }
   ```

---

## Suggested Test Order

1. **Authenticate Users:**
   - Log in as Rahul (`rahul.verma@company.com`).
   - Log in as Priya (`priya.sharma@company.com`).
   - Log in as Anita (`anita.desai@company.com`).
2. **Employee Leave Flow:**
   - Rahul creates a leave request (`POST /api/leaves`).
   - Rahul views his leaves (`GET /api/leaves/my`) and the specific request (`GET /api/leaves/{id}`).
   - Rahul updates the pending leave (`PUT /api/leaves/{id}`).
3. **Validation & Role Checks:**
   - Submit overlapping leave for Rahul (`POST /api/leaves`) -> HTTP 400.
   - Submit invalid date range (`endDate` before `startDate`) -> HTTP 400.
   - Anita (different department) attempts to view Rahul's leave -> HTTP 403.
   - Request without token -> HTTP 401.
   - Invalid ID format -> HTTP 400.
   - Non-existent ID -> HTTP 404.
4. **Manager Approval:**
   - Priya views department leaves (`GET /api/leaves/department`).
   - Priya approves Rahul's leave (`PATCH /api/leaves/{id}/approve`).
5. **Post-Approval Rules & Self-Approval Checks:**
   - Rahul attempts to modify or cancel the approved leave -> HTTP 400.
   - Priya creates her own leave request (`POST /api/leaves`).
   - Priya attempts to approve her own leave -> HTTP 403.
   - Anita (HR) creates a leave request; Priya (Engineering) attempts to approve it -> HTTP 403.
   - Anita cancels her pending leave request (`DELETE /api/leaves/{id}`) -> HTTP 204.

---

## Testing with Postman

A Postman collection is included in the project root:
[`Leave_Management_API.postman_collection.json`](./Leave_Management_API.postman_collection.json)

### Usage

1. Import `Leave_Management_API.postman_collection.json` into Postman.
2. Variables used by the collection:
   - `baseUrl`: defaults to `http://localhost:8080`.
   - `employeeToken`, `managerToken`, `hrToken`: populated by login requests.
   - `leaveId`, `managerLeaveId`, `hrLeaveId`: populated by create requests.
3. Requests can be run in order using the Postman Collection Runner on a fresh application start.
