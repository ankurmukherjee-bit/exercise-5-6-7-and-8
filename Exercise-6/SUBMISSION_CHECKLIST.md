# Submission Checklist — Question 6

Capture your own screenshots after deploying and running the process.

- [ ] Camunda Modeler screenshot showing Start → Approve Leave → End.
- [ ] Show the process ID `leave-request` in Modeler properties.
- [ ] Postman screenshot showing the POST URL, JSON body and successful response.
- [ ] Ensure the response's `processInstanceKey` is visible.
- [ ] Operate screenshot showing the running instance waiting at `Approve Leave`.
- [ ] Operate Variables panel showing `employeeName = Ravi` and `days = 3`.
- [ ] Send the request three times and capture the three process instances in Operate.

## Viva questions

**1. Difference between `processDefinitionId` and `processInstanceKey`?**
- `processDefinitionId` identifies the deployed process model (here, `leave-request`).
- `processInstanceKey` identifies one particular running instance created from that process.

**2. What happens if the Process ID is spelled incorrectly?**
The API cannot find the requested deployed process definition, so the start request fails with an error response. Verify the exact deployed process ID.

**3. Start the process three times. How many instances appear in Operate?**
Three separate process instances should appear, provided all three API calls succeed and none of the instances is completed.
