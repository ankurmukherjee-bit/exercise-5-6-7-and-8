# Camunda 8 API Practice — Question 6
## Start a Camunda 8 Process Using an API Call

This package implements the uploaded SRM practice question.

### Included files
- `bpmn/leave-request.bpmn` — BPMN process: Start Event → User Task "Approve Leave" → End Event.
- `postman/Camunda8_API_Practice_Q6.postman_collection.json` — ready-to-import Postman collection.
- `start-process.curl` — equivalent curl request.
- `SUBMISSION_CHECKLIST.md` — screenshots and viva preparation.

### 1. Open and deploy the BPMN
1. Start Camunda 8 / C8 Run as instructed for your lab environment.
2. Open Camunda Modeler.
3. Open `bpmn/leave-request.bpmn`.
4. Confirm the process ID is exactly `leave-request`.
5. Confirm the process flow is Start → Approve Leave → End.
6. Deploy the process to your Camunda 8 cluster from Modeler.

### 2. Start an instance using Postman
1. Open Postman.
2. Import `postman/Camunda8_API_Practice_Q6.postman_collection.json`.
3. Open **Start Leave Request Process**.
4. Confirm the URL is `POST http://localhost:8080/v2/process-instances`.
5. Confirm `Content-Type: application/json`.
6. Send the request.

Request body:
```json
{
  "processDefinitionId": "leave-request",
  "variables": {
    "employeeName": "Ravi",
    "days": 3
  }
}
```

The response should contain a `processInstanceKey`. Keep that value for your evidence.

### 3. Verify in Operate
1. Open Operate for the same Camunda 8 environment.
2. Find the running `leave-request` process instance.
3. Open the instance.
4. Confirm the token is waiting at **Approve Leave**.
5. Open the Variables panel and confirm:
   - `employeeName` = `Ravi`
   - `days` = `3`

The process is expected to remain active at the user task until the task is completed.

### 4. Start the process three times
Send the same Postman request three times. Each successful call starts a new process instance. Operate should therefore show three instances (assuming all three calls succeed and none has been completed).

### Troubleshooting
- **Process definition not found / wrong ID:** verify the BPMN process ID is exactly `leave-request`, and deploy it before calling the API.
- **Connection refused:** confirm C8 Run is running and listening on `localhost:8080`.
- **Variables missing:** check that the request body uses the exact nested `variables` object and valid JSON.
- **No instance visible in Operate:** confirm Operate is connected to the same Camunda 8 environment used by the API endpoint.

### Scope
This is deliberately the simple process in the practice question. It does not add forms, service tasks, Spring Boot, DMN, or approval logic beyond the single waiting user task.
