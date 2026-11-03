# Exercise 8 — Hospital Appointment Booking (Camunda 8)

This package contains importable Camunda 8 BPMN and DMN models for the attached Exercise 8 scenario.

## Files

```text
Exercise_8_Hospital_Appointment/
├── bpmn/
│   ├── appointment-booking.bpmn       # Parent process
│   └── send-notification.bpmn         # Reusable child process
├── dmn/
│   └── slot_decision.dmn              # UNIQUE hit policy decision table
├── examples/
│   ├── booking-success.json
│   ├── slot-full.json
│   ├── cancellation-message.json
│   └── appointment-completed-message.json
└── docs/
    ├── MODEL_WALKTHROUGH.md
    ├── DEPLOY_AND_TEST.md
    └── VARIABLE_MAPPING_COMPARISON.md
```

## Process IDs and message names

| Item | ID / name |
|---|---|
| Parent process ID | `appointment-booking` |
| Child process ID | `send-notification` |
| DMN decision ID | `slot_decision` |
| Cancellation message | `cancel-booking` |
| Normal completion message | `appointment-completed` |
| Cancellation correlation key | `patientId` |

## Import and deploy

1. Open Camunda Desktop Modeler.
2. Open `bpmn/send-notification.bpmn` and `bpmn/appointment-booking.bpmn`.
3. Open `dmn/slot_decision.dmn`.
4. Deploy all three resources to the same Camunda 8 cluster. Deploy the child process and decision together with the parent so the call activity and Business Rule Task can resolve them.
5. Configure the cluster secret named `TELEGRAM_BOT_TOKEN` with your Telegram bot token. Do not paste the real token into BPMN or commit it to source control.
6. Ensure the Camunda Connectors runtime has the REST Outbound Connector enabled and can reach `https://api.telegram.org`.

## Important worker/runtime requirements

The model includes two application-specific service task types:
- `book-slot`: implement a worker that commits/reserves the requested slot.
- `release-slot`: implement a worker that releases the previously reserved slot.

These are intentionally shown as service tasks because the exercise asks to book and release the slot but does not provide a hospital booking API or worker implementation. Replace these with the actual connector/API tasks if your lab supplies a booking service.

The Telegram notification is implemented using the REST Outbound Connector in the child process. The connector makes:
`POST https://api.telegram.org/bot{{secrets.TELEGRAM_BOT_TOKEN}}/sendMessage`
with JSON body `{ "chat_id": recipientId, "text": messageText }`.

For Telegram, `recipientId` must be a Telegram chat ID (or a valid destination supported by the Bot API), not merely an arbitrary hospital patient database ID. If your exercise's `patientId` is not the Telegram chat ID, store a separate Telegram chat ID and map that value into `recipientId`.

## Why there is an "Await appointment completion" message catch event

The exercise requires a successful booking followed by a cancellation message. A process that ends immediately after sending the confirmation cannot receive a later cancellation. Therefore, the parent model waits at `Await appointment completion` after confirmation. A cancellation message interrupts this wait and triggers the event subprocess. If the appointment proceeds normally, correlate `appointment-completed` using the same `patientId` to finish the parent process.

## Cancellation behaviour

The event subprocess is configured with an **interrupting** message start event. This is a deliberate choice from the two options allowed in the exercise: it stops the normal parent path when cancellation is received, then runs `Release slot` and sends the cancellation notification. If your instructor expects a non-interrupting event subprocess, change the start event's `isInterrupting` attribute to `false`; then ensure the main flow has a separate normal completion path.

## Camunda version

The BPMN/DMN uses Zeebe/Camunda 8 extension elements. Confirm your Desktop Modeler and cluster versions are compatible. The connector task uses the REST connector ID `io.camunda:connector-rest`.
