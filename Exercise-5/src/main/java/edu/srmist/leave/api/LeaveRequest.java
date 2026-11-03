package edu.srmist.leave.api;

/**
 * Incoming JSON payload. Property names intentionally match BPMN process variables.
 * Dates must use ISO format: yyyy-MM-dd.
 */
public record LeaveRequest(
        String studentName,
        String regNo,
        String fromDate,
        String toDate,
        String reason
) {
}
