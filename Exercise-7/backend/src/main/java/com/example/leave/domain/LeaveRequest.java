package com.example.leave.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String employeeName;
    private String employeeEmail;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int days;
    @Column(length = 1000)
    private String reason;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.SUBMITTED;
    @Column(length = 1000)
    private String statusMessage;
    private Long processInstanceKey;
    private Instant createdAt = Instant.now();

    public LeaveRequest() {}

    public Long getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String v) { employeeName = v; }
    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String v) { employeeEmail = v; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String v) { leaveType = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { startDate = v; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate v) { endDate = v; }
    public int getDays() { return days; }
    public void setDays(int v) { days = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { reason = v; }
    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus v) { status = v; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String v) { statusMessage = v; }
    public Long getProcessInstanceKey() { return processInstanceKey; }
    public void setProcessInstanceKey(Long v) { processInstanceKey = v; }
    public Instant getCreatedAt() { return createdAt; }
}
