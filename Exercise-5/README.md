# Student Leave Workflow — Camunda 8 SaaS + Spring Boot

This project implements the **Student Leave Application** lab in the supplied
"Camunda 8 SaaS + Spring Boot — Student Leave Workflow Lab" handout.

## 1. What the process does

1. A student submits a request through `POST /leave`.
2. The `validate-leave` Java job worker calculates inclusive leave days and
   checks that the date range is valid and does not start in the past.
3. An exclusive gateway routes the request:
   - `valid = true` and `leaveDays <= 2`: auto-approved.
   - `valid = true` and `leaveDays > 2`: waits for Faculty Advisor approval in Tasklist.
   - `valid = false`: rejected without Faculty Advisor involvement.
4. The three paths merge and the `notify-student` worker logs the final decision.
5. The process ends with `leaveStatus` set to `APPROVED` or `REJECTED`.

The notification worker logs a message rather than sending a real e-mail, as
specified in the lab.

## 2. Requirements

- Camunda 8 SaaS cluster, generation 8.8
- JDK 17+
- Maven 3.9+
- IntelliJ IDEA or VS Code with Java extensions
- curl or Postman

The project uses Spring Boot 3.5.6 and `camunda-spring-boot-starter` 8.8.15.
Use a Camunda starter version matching the minor version of your cluster.

## 3. Create SaaS credentials

1. Sign in at https://console.camunda.io.
2. Create a cluster (the lab suggests `leave-demo`) using the 8.8 generation.
3. Wait until the cluster status is **Healthy**.
4. Open the cluster's **API** tab and create a client named `leave-app`.
5. Enable the **Zeebe / Orchestration Cluster** scope.
6. Copy the Client ID, Client Secret, Cluster ID and Region from the Spring Boot
   credentials view. Keep the secret private; do not commit it to Git.

## 4. Configure credentials

The file `src/main/resources/application.yaml` reads credentials from environment
variables. Set them in the terminal before starting the app.

### macOS / Linux

```bash
export CAMUNDA_CLIENT_ID="your-client-id"
export CAMUNDA_CLIENT_SECRET="your-client-secret"
export CAMUNDA_CLUSTER_ID="your-cluster-id"
export CAMUNDA_CLUSTER_REGION="your-region"
```

### Windows PowerShell

```powershell
$env:CAMUNDA_CLIENT_ID="your-client-id"
$env:CAMUNDA_CLIENT_SECRET="your-client-secret"
$env:CAMUNDA_CLUSTER_ID="your-cluster-id"
$env:CAMUNDA_CLUSTER_REGION="your-region"
```

Do not add manual gRPC or REST addresses when using SaaS mode; the client derives
the endpoints from the cluster ID and region.

## 5. Project structure

```text
student-leave-workflow/
├── pom.xml
├── requests/
│   ├── short-leave-auto-approved.sh
│   ├── long-leave-faculty-approval.sh
│   └── invalid-past-date.sh
└── src/main/
    ├── java/edu/srmist/leave/
    │   ├── LeaveWorkflowApplication.java
    │   ├── api/
    │   │   ├── LeaveController.java
    │   │   └── LeaveRequest.java
    │   └── worker/
    │       └── LeaveWorkers.java
    └── resources/
        ├── application.yaml
        └── bpmn/
            ├── student-leave.bpmn
            └── fa-approval.form
```

## 6. BPMN model IDs that must match the Java code

| BPMN element | Required value |
|---|---|
| Process ID | `student-leave` |
| Validate service task type | `validate-leave` |
| Notify service task type | `notify-student` |
| Faculty Advisor form ID | `fa-approval-form` |

The application deploys both BPMN and form resources on startup using
`@Deployment`. You do not need to deploy these two resources separately in
Modeler when running this Spring Boot project.

## 7. Run the application

From the project root:

```bash
mvn spring-boot:run
```

The application runs at `http://localhost:8080`. Check the console for successful
startup, deployment and job-worker activation.

## 8. Test the three paths

Use the shell scripts in `requests/`, or copy their JSON into Postman.

### Test A — short leave (auto-approved)

```bash
curl -X POST http://localhost:8080/leave \
  -H "Content-Type: application/json" \
  -d '{"studentName":"Priya","regNo":"RA2311003010001","fromDate":"2026-11-10","toDate":"2026-11-11","reason":"Medical appointment"}'
```

Expected: the process completes without a Faculty Advisor task. The log shows
`APPROVED`.

### Test B — long leave (Faculty Advisor approval)

```bash
curl -X POST http://localhost:8080/leave \
  -H "Content-Type: application/json" \
  -d '{"studentName":"Arun","regNo":"RA2311003010002","fromDate":"2026-11-10","toDate":"2026-11-14","reason":"Family function"}'
```

Expected: the process waits at **Faculty Advisor approval**.

1. Open Tasklist in the Camunda Console.
2. Open the Faculty Advisor approval task.
3. Assign it to yourself.
4. Tick **Approve this leave** to approve, or leave it unticked to reject.
5. Enter remarks and complete the task.
6. The process continues to Notify student and completes.

### Test C — invalid past date

Use `requests/invalid-past-date.sh`.

Expected: the request bypasses Faculty Advisor approval, reaches Notify student
with `approved = false`, and completes with `leaveStatus = REJECTED`.

> The examples use future dates relative to the lab build date. If testing later,
> change the dates to suitable future dates. Dates must be `yyyy-MM-dd`.

## 9. Inspect variables in Operate

Open the process instance in Operate and inspect the Variables panel.

| Variable | Written by | Meaning |
|---|---|---|
| `studentName` | REST request | Student's name |
| `regNo` | REST request | Registration number |
| `fromDate` | REST request | First leave date |
| `toDate` | REST request | Last leave date |
| `reason` | REST request | Leave reason |
| `leaveDays` | `validate-leave` | Inclusive number of days |
| `valid` | `validate-leave` | Whether the dates are valid |
| `approved` | `validate-leave` / Tasklist form | Automatic or Faculty Advisor decision |
| `remarks` | `validate-leave` / Tasklist form | Validation note or advisor remarks |
| `leaveStatus` | `notify-student` | Final `APPROVED` / `REJECTED` status |

## 10. Worker input/output summary

| Worker | Reads | Writes |
|---|---|---|
| `validate-leave` | `fromDate`, `toDate` | `leaveDays`, `valid`, `approved`, `remarks` |
| `notify-student` | `studentName`, `regNo`, `approved`, `remarks` | `leaveStatus` |

## 11. Troubleshooting

- **401 / token error:** check Client ID, Client Secret and the Zeebe /
  Orchestration Cluster scope.
- **Cannot connect / UNAVAILABLE:** verify Cluster ID and Region. Remove manually
  configured endpoint addresses when using SaaS mode.
- **Process not found:** confirm the BPMN process ID is exactly `student-leave`
  and that the Spring Boot app deployed it.
- **Process stuck at a service task:** check that the BPMN task type exactly
  matches the `@JobWorker(type = "...")` value.
- **User task has no form:** check that the form ID is `fa-approval-form` and
  that `fa-approval.form` is included in the `@Deployment` resources.
- **Text cannot be parsed:** check that both dates use `yyyy-MM-dd`.

## 12. Suggested screenshots for submission

1. Operate showing the completed auto-approved instance.
2. Operate showing the token waiting at Faculty Advisor approval.
3. Tasklist showing the Faculty Advisor form.
4. Operate showing a completed rejected instance for invalid dates.
5. Operate Variables panel showing `leaveDays`, `valid`, `approved`,
   `remarks` and `leaveStatus`.

## Scope note

This is the Student Leave Application workflow from the supplied lab handout.
It does not add leave-balance checks, DMN, or a real e-mail integration, because
those are not part of this lab's required workflow.
