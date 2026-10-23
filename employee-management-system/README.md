# Employee Management System

Core Java · JDBC · SQL (PostgreSQL) · Maven — console application with a layered architecture.

```
com.jsp.ems
 ├─ model      Employee, Department, Attendance
 ├─ dao        DAO interfaces  (+ dao/impl  JDBC implementations)
 ├─ service    business rules & validation
 ├─ util       DBConnection (reads db.properties, runs schema.sql)
 ├─ exception  DataAccessException, ValidationException
 └─ Main       menu-driven UI
```

## Schema (normalized, 3NF)
`departments (1) ──< employees (1) ──< attendance`
- Deleting a department sets `employees.dept_id` to NULL (employees become unassigned).
- Deleting an employee cascades to their attendance.
- One attendance row per employee per day (`UNIQUE (emp_id, att_date)`), status limited by a CHECK constraint.

## Run
1. `CREATE DATABASE employee_db;` in PostgreSQL
2. Edit `src/main/resources/db.properties` (username / password)
3. `mvn compile exec:java`  — or run `Main` from IntelliJ. Tables and 4 sample departments are created on first start (`schema.sql`).

## Features
- Employee CRUD, department assignment
- Search by name; filter by department, designation, salary range; list unassigned
- Department CRUD and headcount report (LEFT JOIN + GROUP BY)
- Attendance marking (upsert), view by employee / date / month, monthly summary
