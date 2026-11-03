# Deploy and test — Exercise 8

## Before deploying

- [ ] Camunda 8 cluster is running.
- [ ] REST Outbound Connector runtime is installed and running.
- [ ] Cluster secret `TELEGRAM_BOT_TOKEN` is configured.
- [ ] Telegram bot has permission to message the configured chat.
- [ ] A valid Telegram chat ID is available as `patientId`, or map a dedicated `telegramChatId` into `recipientId`.
- [ ] Implement the `book-slot` and `release-slot` job workers, or connect these tasks to your supplied booking API.
- [ ] Parent, child and DMN resource IDs match exactly.

## Deploy

Deploy these resources to the same cluster:
1. `dmn/slot_decision.dmn`
2. `bpmn/send-notification.bpmn`
3. `bpmn/appointment-booking.bpmn`

The order above is recommended so the decision and called process are already deployed when the parent is started.

## Test A — Cardiology, bookedCount = 3

Start `appointment-booking` with the variables in `examples/booking-success.json`.

Expected:
- `slotDecision.slotStatus` = `AVAILABLE`
- `Book slot` is activated and completed by the `book-slot` worker.
- `Send booking confirmation` starts a child `send-notification` instance.
- Telegram receives the booking confirmation.
- Parent waits at `Await appointment completion`.

## Test B — Cardiology, bookedCount = 12

Start `appointment-booking` with the variables in `examples/slot-full.json`.

Expected:
- `slotDecision.slotStatus` = `FULL`
- Booking task is skipped.
- `Notify slot full` calls `send-notification`.
- Telegram receives the slot-full message.
- Parent reaches `Slot unavailable`.

## Test C — Successful booking followed by cancellation

1. Start with `examples/booking-success.json`.
2. Wait until the parent reaches `Await appointment completion`.
3. Publish/correlate a message named `cancel-booking` with correlation key equal to the exact `patientId`.
4. Include cancellation payload variables if required by your message API; the process already has the patient details in its parent scope.
5. Observe the interrupting event subprocess activate.
6. `Release slot` is completed by the `release-slot` worker.
7. `Send cancellation notification` creates another `send-notification` child instance.
8. Verify Telegram receives the cancellation notification.

## Normal completion (optional)

If the appointment is not cancelled, publish/correlate `appointment-completed` using the same patientId to complete the parent process through the normal end event.

## Operate evidence screenshots

Capture:
1. Parent process instance with the `Check slot` → `Book slot` → confirmation path.
2. The child `send-notification` process instance opened from the call activity.
3. Parent process instance showing the cancellation event subprocess path (`Cancellation received` → `Release slot` → cancellation notification call activity).
4. Optionally, the FULL path for Cardiology with bookedCount = 12.

## Troubleshooting

- **Call activity cannot find process:** verify child process ID is exactly `send-notification` and that it is deployed.
- **Decision not found:** verify decision ID is exactly `slot_decision` and that the DMN is deployed.
- **No matching decision rule:** ensure `bookedCount` is numeric and not a quoted string.
- **Cancellation is not caught:** verify the message name `cancel-booking`, correlation key expression `= patientId`, and that the parent instance is still active at the wait state.
- **Telegram 401:** verify the secret name/value and bot token.
- **Telegram 400:** verify `recipientId` is a valid Telegram chat ID and body contains `chat_id` and `text`.
- **Connector job remains active:** verify the REST connector runtime is running and the connector task uses `io.camunda:connector-rest`.
- **Book/release task remains active:** implement and start workers for `book-slot` and `release-slot`.
