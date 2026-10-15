-- Employee Leave Management Schema for PostgreSQL

DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;

-- 1. Departments table
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- 2. Employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE CASCADE
);

-- 3. Leave Requests table
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed Data:
-- Plain-text password for all sample users: password123
-- Verified BCrypt hash: $2a$10$vNNrjDnyeRMiBEuJECyDiOb3WbvNoBY72ZG97Fh3yEYA9DlruSo3e

INSERT INTO departments (id, name) VALUES
    (1, 'Engineering'),
    (2, 'Human Resources');

INSERT INTO employees (id, name, email, password, role, department_id) VALUES
    (1, 'Priya Sharma', 'priya.sharma@company.com', '$2a$10$vNNrjDnyeRMiBEuJECyDiOb3WbvNoBY72ZG97Fh3yEYA9DlruSo3e', 'MANAGER', 1),
    (2, 'Rahul Verma', 'rahul.verma@company.com', '$2a$10$vNNrjDnyeRMiBEuJECyDiOb3WbvNoBY72ZG97Fh3yEYA9DlruSo3e', 'EMPLOYEE', 1),
    (3, 'Anita Desai', 'anita.desai@company.com', '$2a$10$vNNrjDnyeRMiBEuJECyDiOb3WbvNoBY72ZG97Fh3yEYA9DlruSo3e', 'EMPLOYEE', 2);

-- Reset sequence counters so future auto-generated IDs start after pre-seeded rows
SELECT setval('departments_id_seq', (SELECT MAX(id) FROM departments));
SELECT setval('employees_id_seq', (SELECT MAX(id) FROM employees));
