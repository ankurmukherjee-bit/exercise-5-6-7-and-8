# Variable propagation comparison

The exercise asks to compare propagating all parent variables with switching propagation off and mapping only the variables the child needs.

| Call activity | Parent-to-child propagation | Explicit input mappings | What the child receives |
|---|---|---|---|
| Send booking confirmation | `propagateAllParentVariables=true` | `recipientId`, `messageText` | All parent variables plus the two notification variables |
| Notify slot full | `propagateAllParentVariables=false` | `recipientId`, `messageText` | Only the two mapped notification variables |
| Send cancellation notification | `propagateAllParentVariables=false` | `recipientId`, `messageText` | Only the two mapped notification variables |

## Interpretation

**Propagate all variables ON**
- Quick to configure.
- Child process can access the parent's complete process context.
- Can copy unrelated or sensitive data into the child process.
- Makes the child more tightly coupled to the parent data model.

**Propagate all variables OFF**
- Explicitly maps only `recipientId` and `messageText`.
- Reduces unnecessary data sharing.
- Makes the reusable child process independent of parent-specific variables.
- Requires every needed input to be mapped deliberately.

The child process does not need `patientName`, `department`, `preferredSlot`, `bookedCount` or `slotDecision`; it only needs `recipientId` and `messageText`.

This exercise demonstrates parent-to-child propagation. Child-to-parent propagation is separately controlled by `propagateAllChildVariables`; it is disabled in these call activities because the parent does not need a notification result.
