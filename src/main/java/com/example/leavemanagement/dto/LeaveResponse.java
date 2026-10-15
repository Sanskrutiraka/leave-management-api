package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveStatus;
import com.example.leavemanagement.entity.LeaveType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveResponse {

    private Long id;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;

    public LeaveResponse() {
    }

    public LeaveResponse(Long id, LeaveType type, LocalDate startDate, LocalDate endDate,
                         String reason, LeaveStatus status, Long employeeId, String employeeName,
                         String employeeEmail, Long departmentId, String departmentName,
                         LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.createdAt = createdAt;
    }

    public static LeaveResponse fromEntity(LeaveRequest leave) {
        Long empId = null;
        String empName = null;
        String empEmail = null;
        Long deptId = null;
        String deptName = null;

        if (leave.getEmployee() != null) {
            empId = leave.getEmployee().getId();
            empName = leave.getEmployee().getName();
            empEmail = leave.getEmployee().getEmail();
            if (leave.getEmployee().getDepartment() != null) {
                deptId = leave.getEmployee().getDepartment().getId();
                deptName = leave.getEmployee().getDepartment().getName();
            }
        }

        return new LeaveResponse(
                leave.getId(),
                leave.getType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                empId,
                empName,
                empEmail,
                deptId,
                deptName,
                leave.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
