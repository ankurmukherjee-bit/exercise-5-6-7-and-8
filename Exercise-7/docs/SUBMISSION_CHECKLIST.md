# Submission checklist — Leave Application System

## Step 1 — BPMN
- [ ] BPMN process ID is `leave-request`; executable is enabled.
- [ ] Start → Validate leave request → Request valid? gateway.
- [ ] Valid path reaches Manager approval; invalid default path goes to rejection merge.
- [ ] Manager approval form ID is `leave-approval`.
- [ ] Form keys are `approved` and `managerComment`.
- [ ] Approved and rejected finalisation service tasks use `finalize-leave`.
- [ ] End events are Leave approved and Leave rejected.

## Step 2 — React
- [ ] Vite React + TypeScript frontend runs on port 5173.
- [ ] Apply view submits a leave request.
- [ ] My Requests displays requests and annual balance.
- [ ] Manager Inbox displays pending requests and supports approve/reject comments.
- [ ] Vite proxies `/api` to `http://localhost:8081`.

## Step 3 — Spring Boot
- [ ] Java 21 and Maven project builds.
- [ ] Camunda starter version matches the cluster.
- [ ] BPMN and form are deployed from backend resources.
- [ ] H2 database persists leave requests and balances.
- [ ] Workers match task types `validate-leave` and `finalize-leave`.
- [ ] REST endpoints match the React API contract.

## Step 4 — Integration
- [ ] Camunda, backend and frontend start successfully.
- [ ] Submit request works and returns HTTP 201.
- [ ] Employee can see own requests and balance.
- [ ] Valid requests wait at Manager approval.
- [ ] Manager approve and reject actions work.
- [ ] Insufficient balance and weekend-only requests are rejected.
- [ ] Operate shows expected process paths and variables.
- [ ] H2 records agree with frontend and Operate.

## Step 5 — Docker / Exercise 7
- [ ] Build application images with `docker compose build`.
- [ ] Start database, backend and frontend using `docker compose up -d`.
- [ ] Frontend is reachable at `http://localhost:3000`.
- [ ] Backend is reachable at `http://localhost:8081`.
- [ ] Backend container can reach the separately running Camunda cluster.
- [ ] Take screenshots of the complete application and Docker containers.

## Suggested evidence
1. BPMN diagram in Camunda Modeler.
2. Manager approval form in Modeler.
3. React Apply screen.
4. My Requests screen with request status and leave balance.
5. Manager Inbox with approve/reject controls.
6. Operate showing approved process path.
7. Operate showing rejected process path.
8. H2 Console showing leave request and leave balance records.
9. Docker Desktop / `docker compose ps` showing containers.
