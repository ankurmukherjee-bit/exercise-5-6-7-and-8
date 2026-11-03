# API testing — Step 4

Base URL: `http://localhost:8081`

## Submit leave

`POST /api/leaves`

```json
{
  "employeeName": "Alice",
  "employeeEmail": "alice@example.com",
  "leaveType": "ANNUAL",
  "startDate": "2026-11-10",
  "endDate": "2026-11-12",
  "reason": "Personal leave"
}
```

Expected: HTTP 201 and a LeaveRequest JSON object. Use future dates when you run the test.

## List requests

`GET /api/leaves?employeeEmail=alice%40example.com`

Expected: array of Alice's requests.

## Manager inbox

`GET /api/leaves/pending`

Expected: array with status `PENDING_APPROVAL`.

## Balance

`GET /api/leaves/balance?employeeEmail=alice%40example.com`

Expected:

```json
{ "remainingDays": 20 }
```

## Approve / reject

`POST /api/leaves/{id}/decision`

```json
{
  "approved": true,
  "comment": "Approved. Please coordinate handover."
}
```

Expected: HTTP 204. Set `approved` to `false` to reject.

## Required test scenarios

| Scenario | Steps | Expected result |
|---|---|---|
| A. Approved request | Submit annual leave for three weekdays; approve in Manager Inbox | APPROVED; balance decreases by 3 from 20 |
| B. Manager rejection | Submit another valid request; reject with comment | REJECTED; balance unchanged |
| C. Insufficient balance | Request more annual leave days than remaining balance | Rejected before manager approval; validation message shown |
| D. Weekend-only | Submit Saturday through Sunday | Rejected because there are zero working days |
| E. Tasklist approval | Submit valid request and complete Manager approval in Tasklist | Process completes; final status APPROVED |
| F. Invalid dates | Try an end date earlier than start date via API | Invalid request cannot be approved; no deduction |

## Inspect in Operate

Open `http://localhost:8080/operate`, select the process instance and inspect:
`requestId`, `valid`, `validationMessage`, `approved`, `managerComment`, and `outcome`.

## H2 Console (local profile)

Open `http://localhost:8081/h2-console`

- JDBC URL: `jdbc:h2:file:./data/leavedb`
- Username: `sa`
- Password: empty
