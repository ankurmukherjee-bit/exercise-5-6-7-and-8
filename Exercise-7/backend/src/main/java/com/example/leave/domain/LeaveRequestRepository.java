package com.example.leave.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeEmailOrderByCreatedAtDesc(String employeeEmail);
    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveStatus status);
}
