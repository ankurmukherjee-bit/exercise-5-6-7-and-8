package edu.srmist.leave.worker;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class LeaveWorkers {

    private static final Logger log = LoggerFactory.getLogger(LeaveWorkers.class);

    /**
     * Called by the BPMN service task whose Zeebe task type is "validate-leave".
     * Computes inclusive leave days, rejects past/reversed date ranges, and
     * auto-approves valid requests of two days or fewer.
     */
    @JobWorker(type = "validate-leave")
    public Map<String, Object> validateLeave(
            @Variable String fromDate,
            @Variable String toDate
    ) {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);

        long days = ChronoUnit.DAYS.between(from, to) + 1;
        boolean valid = days > 0 && !from.isBefore(LocalDate.now());
        boolean autoApproved = valid && days <= 2;

        String remarks;
        if (!valid) {
            remarks = "Rejected: leave dates are invalid or start in the past.";
        } else if (autoApproved) {
            remarks = "Automatically approved (two days or fewer).";
        } else {
            remarks = "Forwarded to Faculty Advisor for approval.";
        }

        log.info("Validated leave request: from={}, to={}, leaveDays={}, valid={}, autoApproved={}",
                fromDate, toDate, days, valid, autoApproved);

        return Map.of(
                "leaveDays", days,
                "valid", valid,
                "approved", autoApproved,
                "remarks", remarks
        );
    }

    /**
     * Called after the three routing paths merge. For long leave, the
     * Faculty Advisor form supplies approved and may replace remarks.
     */
    @JobWorker(type = "notify-student")
    public Map<String, Object> notifyStudent(
            @Variable String studentName,
            @Variable String regNo,
            @Variable Boolean approved,
            @Variable String remarks
    ) {
        String status = Boolean.TRUE.equals(approved) ? "APPROVED" : "REJECTED";

        // Lab stand-in for e-mail/SMS: the decision is written to the application log.
        log.info("Mail to {} ({}): your leave is {}. Remarks: {}",
                studentName, regNo, status, remarks);

        return Map.of("leaveStatus", status);
    }
}
