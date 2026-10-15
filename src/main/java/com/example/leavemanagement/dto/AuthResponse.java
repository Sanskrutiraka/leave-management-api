package com.example.leavemanagement.dto;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private EmployeeResponse employee;

    public AuthResponse() {
    }

    public AuthResponse(String token, EmployeeResponse employee) {
        this.token = token;
        this.employee = employee;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public EmployeeResponse getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeResponse employee) {
        this.employee = employee;
    }
}
