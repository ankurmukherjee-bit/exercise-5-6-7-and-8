# Model walkthrough

## 1. Child process — `send-notification`

Sequence:
1. Start event `Notification requested`.
2. REST connector task `Send Telegram message`.
3. End event `Notification sent`.

Inputs expected by the child process:
- `recipientId`
- `messageText`

The connector URL uses the secret placeholder `{{secrets.TELEGRAM_BOT_TOKEN}}`. The message body is mapped from the two child variables.

## 2. DMN — `slot_decision`

- Hit policy: `UNIQUE`
- Input 1: `department` (`string`)
- Input 2: `bookedCount` (`number`)
- Output 1: `slotStatus` (`string`)
- Output 2: `suggestedAction` (`string`)

Rules:
- `bookedCount < 10` → `AVAILABLE`, `BOOK_SLOT`
- `bookedCount >= 10` → `FULL`, `OFFER_ALTERNATIVE_SLOT`

The department input is retained as a decision input. The sample capacity rule is applied to all departments; in particular, Cardiology with 12 booked appointments returns FULL and Cardiology with 3 returns AVAILABLE. To model department-specific capacities, add mutually exclusive rules per department while preserving UNIQUE hit-policy semantics.

## 3. Parent process — `appointment-booking`

Normal path:
1. Start `Booking requested`.
2. Business Rule Task `Check slot` evaluates `slot_decision` into `slotDecision`.
3. Exclusive gateway `Slot available?`.
4. AVAILABLE → service task `Book slot` → call activity `Send booking confirmation` → message catch `Await appointment completion`.
5. FULL → call activity `Notify slot full` → end event `Slot unavailable`.

Cancellation event subprocess:
1. Message start event `Cancellation received`, message name `cancel-booking`, correlation key `= patientId`.
2. Service task `Release slot`.
3. Call activity `Send cancellation notification`.

The cancellation event subprocess is interrupting so it cancels the normal parent path and performs release + notification.

## 4. Parent variables

Start the parent process with:
- `patientId`
- `patientName`
- `department`
- `preferredSlot`
- `bookedCount`

The DMN result is stored in `slotDecision`, with fields `slotStatus` and `suggestedAction`.

The successful booking path also builds `messageText` and maps `patientId` to `recipientId`. The full and cancellation paths map only the child process inputs.

## 5. Connector task details

REST method: `POST`

URL:
`https://api.telegram.org/bot{{secrets.TELEGRAM_BOT_TOKEN}}/sendMessage`

Body:
```json
{
  "chat_id": "recipientId",
  "text": "messageText"
}
```

In the BPMN connector input mapping, the body is a FEEL context expression using the variables, not the literal strings shown above.
