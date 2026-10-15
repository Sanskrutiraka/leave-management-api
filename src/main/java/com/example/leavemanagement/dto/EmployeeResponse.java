package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.Role;

public class EmployeeResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long departmentId;
    private String departmentName;

    public EmployeeResponse() {
    }

    public EmployeeResponse(Long id, String name, String email, Role role, Long departmentId, String departmentName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public static EmployeeResponse fromEntity(Employee employee) {
        Long deptId = null;
        String deptName = null;
        if (employee.getDepartment() != null) {
            deptId = employee.getDepartment().getId();
            deptName = employee.getDepartment().getName();
        }
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole(),
                deptId,
                deptName
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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
}
