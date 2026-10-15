package com.example.leavemanagement.config;

import com.example.leavemanagement.entity.Department;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.repository.DepartmentRepository;
import com.example.leavemanagement.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DepartmentRepository departmentRepository,
                           EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            Department engineering = departmentRepository.save(new Department("Engineering"));
            Department hr = departmentRepository.save(new Department("Human Resources"));

            // Pre-seed sample users (Password for all: password123)
            String encodedPassword = passwordEncoder.encode("password123");

            Employee manager = new Employee(
                    "Priya Sharma",
                    "priya.sharma@company.com",
                    encodedPassword,
                    Role.MANAGER,
                    engineering
            );
            employeeRepository.save(manager);

            Employee rahul = new Employee(
                    "Rahul Verma",
                    "rahul.verma@company.com",
                    encodedPassword,
                    Role.EMPLOYEE,
                    engineering
            );
            employeeRepository.save(rahul);

            Employee anita = new Employee(
                    "Anita Desai",
                    "anita.desai@company.com",
                    encodedPassword,
                    Role.EMPLOYEE,
                    hr
            );
            employeeRepository.save(anita);
        }
    }
}
