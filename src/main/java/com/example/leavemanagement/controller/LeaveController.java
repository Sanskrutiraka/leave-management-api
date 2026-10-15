package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.LeaveRequestDto;
import com.example.leavemanagement.dto.LeaveResponse;
import com.example.leavemanagement.entity.LeaveStatus;
import com.example.leavemanagement.service.LeaveService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // 1. Employee: Create a leave request
    @PostMapping
    public ResponseEntity<LeaveResponse> createLeaveRequest(@Valid @RequestBody LeaveRequestDto dto) {
        LeaveResponse response = leaveService.createLeaveRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Employee: View own leave requests
    @GetMapping("/my")
    public ResponseEntity<List<LeaveResponse>> getMyLeaveRequests() {
        List<LeaveResponse> leaves = leaveService.getMyLeaveRequests();
        return ResponseEntity.ok(leaves);
    }

    // 3. View a single leave request (Owner or Manager of the department)
    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponse> getLeaveRequestById(@PathVariable Long id) {
        LeaveResponse response = leaveService.getLeaveRequestById(id);
        return ResponseEntity.ok(response);
    }

    // 4. Employee: Update own leave request while PENDING
    @PutMapping("/{id}")
    public ResponseEntity<LeaveResponse> updateLeaveRequest(@PathVariable Long id,
                                                           @Valid @RequestBody LeaveRequestDto dto) {
        LeaveResponse response = leaveService.updateLeaveRequest(id, dto);
        return ResponseEntity.ok(response);
    }

    // 5. Employee: Cancel own leave request while PENDING
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelLeaveRequest(@PathVariable Long id) {
        leaveService.cancelLeaveRequest(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Manager: View all requests in manager's own department
    @GetMapping("/department")
    public ResponseEntity<List<LeaveResponse>> getDepartmentLeaveRequests() {
        List<LeaveResponse> leaves = leaveService.getDepartmentLeaveRequests();
        return ResponseEntity.ok(leaves);
    }

    // 7. Manager: Approve leave request
    @PatchMapping("/{id}/approve")
    public ResponseEntity<LeaveResponse> approveLeaveRequest(@PathVariable Long id) {
        LeaveResponse response = leaveService.updateLeaveStatus(id, LeaveStatus.APPROVED);
        return ResponseEntity.ok(response);
    }

    // 8. Manager: Reject leave request
    @PatchMapping("/{id}/reject")
    public ResponseEntity<LeaveResponse> rejectLeaveRequest(@PathVariable Long id) {
        LeaveResponse response = leaveService.updateLeaveStatus(id, LeaveStatus.REJECTED);
        return ResponseEntity.ok(response);
    }
}
