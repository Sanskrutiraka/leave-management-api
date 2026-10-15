package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.LeaveRequestDto;
import com.example.leavemanagement.dto.LeaveResponse;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveStatus;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.exception.ResourceNotFoundException;
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    private Employee getAuthenticatedEmployee() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated employee not found"));
    }

    @Transactional
    public LeaveResponse createLeaveRequest(LeaveRequestDto dto) {
        Employee employee = getAuthenticatedEmployee();

        // Business rule 1: End date must not be before start date
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        // Business rule 2: No overlapping leaves for the same employee (-1L because no existing leave to exclude)
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                employee.getId(),
                dto.getStartDate(),
                dto.getEndDate(),
                -1L
        );
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Employee already has an overlapping leave request for these dates");
        }

        LeaveRequest leaveRequest = new LeaveRequest(
                dto.getType(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getReason(),
                LeaveStatus.PENDING,
                employee
        );

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return LeaveResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaveRequests() {
        Employee employee = getAuthenticatedEmployee();
        List<LeaveRequest> leaves = leaveRequestRepository.findByEmployeeId(employee.getId());

        List<LeaveResponse> responses = new ArrayList<>();
        for (LeaveRequest leave : leaves) {
            responses.add(LeaveResponse.fromEntity(leave));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public LeaveResponse getLeaveRequestById(Long id) {
        Employee currentEmployee = getAuthenticatedEmployee();

        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        boolean isOwner = leave.getEmployee().getId().equals(currentEmployee.getId());
        boolean isDepartmentManager = currentEmployee.getRole() == Role.MANAGER
                && leave.getEmployee().getDepartment().getId().equals(currentEmployee.getDepartment().getId());

        if (!isOwner && !isDepartmentManager) {
            throw new AccessDeniedException("You do not have permission to view this leave request");
        }

        return LeaveResponse.fromEntity(leave);
    }

    @Transactional
    public LeaveResponse updateLeaveRequest(Long id, LeaveRequestDto dto) {
        Employee currentEmployee = getAuthenticatedEmployee();

        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        // Authorization check: Only the owner can update their request
        if (!leave.getEmployee().getId().equals(currentEmployee.getId())) {
            throw new AccessDeniedException("You can only modify your own leave requests");
        }

        // Business rule 3: Cannot modify a leave request that is no longer PENDING
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Cannot modify a leave request that is no longer PENDING");
        }

        // Business rule 1: End date must not be before start date
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        // Business rule 2: Check for overlapping leaves, excluding current request ID
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                currentEmployee.getId(),
                dto.getStartDate(),
                dto.getEndDate(),
                leave.getId()
        );
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Employee already has an overlapping leave request for these dates");
        }

        leave.setType(dto.getType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setReason(dto.getReason());

        LeaveRequest updated = leaveRequestRepository.save(leave);
        return LeaveResponse.fromEntity(updated);
    }

    @Transactional
    public void cancelLeaveRequest(Long id) {
        Employee currentEmployee = getAuthenticatedEmployee();

        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        // Authorization check: Only the owner can cancel their request
        if (!leave.getEmployee().getId().equals(currentEmployee.getId())) {
            throw new AccessDeniedException("You can only cancel your own leave requests");
        }

        // Business rule 3: Cannot cancel a request that is no longer PENDING
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Cannot cancel a leave request that is no longer PENDING");
        }

        leaveRequestRepository.delete(leave);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getDepartmentLeaveRequests() {
        Employee currentEmployee = getAuthenticatedEmployee();

        // Role check: Only managers can view department requests
        if (currentEmployee.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Only managers can view department leave requests");
        }

        Long departmentId = currentEmployee.getDepartment().getId();
        List<LeaveRequest> leaves = leaveRequestRepository.findByEmployeeDepartmentId(departmentId);

        List<LeaveResponse> responses = new ArrayList<>();
        for (LeaveRequest leave : leaves) {
            responses.add(LeaveResponse.fromEntity(leave));
        }
        return responses;
    }

    @Transactional
    public LeaveResponse updateLeaveStatus(Long id, LeaveStatus newStatus) {
        Employee currentEmployee = getAuthenticatedEmployee();

        // Role check: Only managers can approve or reject
        if (currentEmployee.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Only managers can approve or reject leave requests");
        }

        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        // Self-decision check: Managers cannot approve or reject their own leave requests
        if (leave.getEmployee().getId().equals(currentEmployee.getId())) {
            throw new AccessDeniedException("Managers cannot approve or reject their own leave requests");
        }

        // Department check: Managers can only approve/reject within their own department
        if (!leave.getEmployee().getDepartment().getId().equals(currentEmployee.getDepartment().getId())) {
            throw new AccessDeniedException("Managers can only approve or reject leave requests within their own department");
        }

        // Can only decide on PENDING requests
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Cannot update status of a leave request that is already " + leave.getStatus());
        }

        if (newStatus != LeaveStatus.APPROVED && newStatus != LeaveStatus.REJECTED) {
            throw new BadRequestException("Status must be either APPROVED or REJECTED");
        }

        leave.setStatus(newStatus);
        LeaveRequest saved = leaveRequestRepository.save(leave);
        return LeaveResponse.fromEntity(saved);
    }
}
