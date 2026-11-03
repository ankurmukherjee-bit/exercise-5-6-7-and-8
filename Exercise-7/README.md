# Leave Application System — Full-Stack Exercise

This project is assembled from the uploaded SRM Leave Application System guides:
- Step 1: BPMN process and manager approval form
- Step 2: React + Vite + TypeScript frontend
- Step 3: Spring Boot + Camunda 8 backend
- Step 4: integration and testing
- Exercise 7 architecture image: frontend/backend/database container layout

## Architecture and local ports

| Component | Port | Responsibility |
|---|---:|---|
| React + Vite | 5173 | Apply, My Requests, Manager Inbox |
| Spring Boot | 8081 | REST API, persistence, Camunda client and job workers |
| Camunda 8 Self-Managed | 8080 REST/UI; 26500 gRPC | BPMN execution, Operate, Tasklist |
| H2 | embedded | Local development database |
| PostgreSQL | 5432 | Optional Docker Compose database |

React calls only Spring Boot through `/api`; it does not call Camunda directly.

## Project layout

```text
leave-system/
├── bpmn/
│   ├── leave-request.bpmn
│   └── leave-approval.form
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/leave/
│       │   ├── LeaveApplication.java
│       │   ├── api/
│       │   ├── domain/
│       │   ├── service/
│       │   └── worker/
│       └── resources/
│           ├── application.yml
│           └── bpmn/
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
├── docker/
│   ├── backend.Dockerfile
│   └── frontend.Dockerfile
├── docker-compose.yml
└── docs/
    ├── API_TESTS.md
    └── SUBMISSION_CHECKLIST.md
```

## Before running

1. Install Java 21, Maven 3.9+, Node.js 20+, npm and Docker.
2. Start a compatible Camunda 8 Self-Managed distribution. The supplied backend guide expects:
   - REST: `http://localhost:8080`
   - gRPC: `http://localhost:26500`
3. Confirm Camunda is healthy before starting the backend.
4. The backend guide uses Camunda starter `8.8.9` as an example. The separate uploaded dependency sheet shows `camunda-spring-boot-3-starter` `8.9.12`. These are different version lines/artifact names. This generated project consistently uses the Step 3 backend guide's `camunda-spring-boot-starter:8.8.9`. If your Camunda cluster is 8.9, switch to the matching starter/artifact and verify imports/API methods against that version; do not mix them.

## Run locally

Local profile uses embedded H2. Docker Compose overrides the datasource settings to use the PostgreSQL service and includes the PostgreSQL JDBC driver.

### 1. Start Camunda

Start your Camunda 8 Self-Managed Docker Compose distribution and wait for services to become healthy. This project does not bundle Camunda's official platform compose file.

### 2. Start Spring Boot backend

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

Backend: `http://localhost:8081`

### 3. Start React frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

The Vite proxy forwards `/api` to Spring Boot at `http://localhost:8081`.

## Workflow

1. Employee submits a leave request.
2. Backend stores it, computes weekdays, initializes a default 20-day balance if needed, and starts process `leave-request`.
3. `validate-leave` worker validates the date range, weekend-only requests and annual leave balance.
4. Invalid requests route to rejection finalization. Valid requests wait at Manager approval.
5. Manager approves or rejects with a comment.
6. `finalize-leave` worker updates the status; annual balance is deducted only for approved annual leave.
7. My Requests refreshes and Operate shows the executed BPMN path.

## API endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/leaves` | Submit leave request |
| GET | `/api/leaves?employeeEmail=...` | List employee requests |
| GET | `/api/leaves/pending` | Manager inbox |
| GET | `/api/leaves/balance?employeeEmail=...` | Annual leave balance |
| POST | `/api/leaves/{id}/decision` | Approve/reject |

See `docs/API_TESTS.md` for sample requests and scenarios.

## Important scope and security notes

This is a classroom MVP, matching the uploaded guides. It does not implement authentication or role-based authorisation. Anyone who can reach the manager inbox/decision endpoint could act as a manager. Do not expose it publicly or use it for real employee data without authentication, authorisation, audit controls, secure secrets, backups and production database configuration.

The uploaded Step 3 guide describes the LeaveService as an implementation outline and says exact Camunda Java client calls depend on the installed starter version. The Camunda task-search/completion calls in this project are written against the 8.8 Java client API shape; if your cluster/starter differs, adjust those methods to that version's API.
