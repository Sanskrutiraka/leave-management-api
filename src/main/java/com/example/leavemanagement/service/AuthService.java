package com.example.leavemanagement.service;

import com.example.leavemanagement.config.JwtUtil;
import com.example.leavemanagement.dto.AuthResponse;
import com.example.leavemanagement.dto.EmployeeResponse;
import com.example.leavemanagement.dto.LoginRequest;
import com.example.leavemanagement.dto.RegisterRequest;
import com.example.leavemanagement.entity.Department;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.exception.ResourceNotFoundException;
import com.example.leavemanagement.repository.DepartmentRepository;
import com.example.leavemanagement.repository.EmployeeRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(EmployeeRepository employeeRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate with Spring Security's AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + request.getEmail()));

        String token = jwtUtil.generateToken(employee.getEmail(), employee.getRole().name());
        return new AuthResponse(token, EmployeeResponse.fromEntity(employee));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // Registration always creates an EMPLOYEE (ignores any role in the request)
        Employee employee = new Employee(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.EMPLOYEE,
                department
        );

        Employee saved = employeeRepository.save(employee);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name());

        return new AuthResponse(token, EmployeeResponse.fromEntity(saved));
    }
}
