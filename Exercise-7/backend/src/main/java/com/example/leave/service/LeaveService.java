package com.example.leave.service;

import com.example.leave.api.DecisionRequest;
import com.example.leave.api.SubmitLeaveRequest;
import com.example.leave.domain.*;
import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.response.UserTask;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class LeaveService {
    private static final int DEFAULT_ANNUAL_BALANCE = 20;

    private final LeaveRequestRepository requests;
    private final LeaveBalanceRepository balances;
    private final CamundaClient camunda;

    public LeaveService(LeaveRequestRepository requests,
                        LeaveBalanceRepository balances,
                        CamundaClient camunda) {
        this.requests = requests;
        this.balances = balances;
        this.camunda = camunda;
    }

    @Transactional
    public LeaveRequest submit(SubmitLeaveRequest body) {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeName(body.employeeName());
        request.setEmployeeEmail(body.employeeEmail().toLowerCase());
        request.setLeaveType(body.leaveType().toUpperCase());
        request.setStartDate(body.startDate());
        request.setEndDate(body.endDate());
        request.setReason(body.reason());
        request.setDays(countWeekdays(body.startDate(), body.endDate()));
        request.setStatus(LeaveStatus.SUBMITTED);
        request.setStatusMessage("Submitted; awaiting automatic validation.");
        request = requests.save(request);

        // Initialise an example 20-day entitlement for a new employee.
        balance(request.getEmployeeEmail());

        Map<String, Object> variables = new HashMap<>();
        variables.put("requestId", request.getId());
        variables.put("employeeEmail", request.getEmployeeEmail());
        variables.put("leaveType", request.getLeaveType());
        variables.put("days", request.getDays());
        variables.put("startDate", request.getStartDate().toString());
        variables.put("endDate", request.getEndDate().toString());

        ProcessInstanceEvent instance = camunda.newCreateInstanceCommand()
            .bpmnProcessId("leave-request")
            .latestVersion()
            .variables(variables)
            .send()
            .join();

        request.setProcessInstanceKey(instance.getProcessInstanceKey());
        return requests.save(request);
    }

    @Transactional
    public Map<String, Object> validate(Long requestId) {
        LeaveRequest request = requests.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + requestId));

        boolean valid = true;
        String message = "Validation passed.";

        if (request.getEndDate().isBefore(request.getStartDate())) {
            valid = false;
            message = "Rejected: end date must not be before start date.";
        } else if (request.getStartDate().isBefore(LocalDate.now())) {
            valid = false;
            message = "Rejected: leave start date cannot be in the past.";
        } else if (request.getDays() <= 0) {
            valid = false;
            message = "Rejected: selected period contains no working days.";
        } else if ("ANNUAL".equals(request.getLeaveType())
                && request.getDays() > balance(request.getEmployeeEmail()).getRemainingDays()) {
            valid = false;
            message = "Rejected: insufficient annual leave balance.";
        }

        if (valid) {
            request.setStatus(LeaveStatus.PENDING_APPROVAL);
            request.setStatusMessage("Validated; waiting for manager approval.");
        } else {
            request.setStatusMessage(message);
        }
        requests.save(request);

        return Map.of("valid", valid, "validationMessage", message);
    }

    @Transactional
    public void decide(Long id, DecisionRequest decision) {
        LeaveRequest request = requests.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));

        if (request.getStatus() != LeaveStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Request is not awaiting manager approval.");
        }
        if (request.getProcessInstanceKey() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Process instance key is not available yet.");
        }

        // Camunda user-task search index may lag briefly after the BPMN user task activates.
        var search = camunda.newUserTaskSearchRequest()
            .filter(f -> f.processInstanceKey(request.getProcessInstanceKey()))
            .send().join();

        if (search.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Manager approval task is not indexed yet. Retry shortly.");
        }

        UserTask task = search.items().get(0);
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", decision.approved());
        variables.put("managerComment", decision.comment() == null ? "" : decision.comment());

        camunda.newCompleteUserTaskCommand(task.getUserTaskKey())
            .variables(variables)
            .send().join();
    }

    @Transactional
    public void finalizeRequest(Long id, boolean approved, String managerComment,
                                String validationMessage) {
        LeaveRequest request = requests.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + id));

        if (approved) {
            boolean wasAlreadyApproved = request.getStatus() == LeaveStatus.APPROVED;
            request.setStatus(LeaveStatus.APPROVED);
            request.setStatusMessage(managerComment == null || managerComment.isBlank()
                ? "Approved by manager." : managerComment);
            if ("ANNUAL".equals(request.getLeaveType()) && !wasAlreadyApproved) {
                LeaveBalance balance = balance(request.getEmployeeEmail());
                // Do not deduct twice if Camunda retries the finalisation job.
                balance.setRemainingDays(Math.max(0, balance.getRemainingDays() - request.getDays()));
                balances.save(balance);
            }
        } else {
            request.setStatus(LeaveStatus.REJECTED);
            String reason = managerComment == null || managerComment.isBlank()
                ? validationMessage : managerComment;
            request.setStatusMessage(reason == null || reason.isBlank() ? "Rejected." : reason);
        }
        requests.save(request);
        notifyStudent(request);
    }

    @Transactional
    public LeaveBalance balance(String email) {
        String normalized = email.toLowerCase();
        return balances.findById(normalized)
            .orElseGet(() -> balances.save(new LeaveBalance(normalized, DEFAULT_ANNUAL_BALANCE)));
    }

    private int countWeekdays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) return 0;
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) count++;
        }
        return count;
    }

    private void notifyStudent(LeaveRequest request) {
        // Classroom notification hook: replace with mail/SMS integration if required.
        System.out.printf("Notification: %s (%s), leave request #%d is %s — %s%n",
            request.getEmployeeName(), request.getEmployeeEmail(), request.getId(),
            request.getStatus(), request.getStatusMessage());
    }
}
