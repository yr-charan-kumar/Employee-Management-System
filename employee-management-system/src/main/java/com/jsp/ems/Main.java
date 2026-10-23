package com.jsp.ems;

import com.jsp.ems.exception.DataAccessException;
import com.jsp.ems.exception.ValidationException;
import com.jsp.ems.model.Attendance;
import com.jsp.ems.model.Department;
import com.jsp.ems.model.Employee;
import com.jsp.ems.service.AttendanceService;
import com.jsp.ems.service.DepartmentService;
import com.jsp.ems.service.EmployeeService;
import com.jsp.ems.util.DBConnection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Scanner in = new Scanner(System.in);
    private static final EmployeeService employees = new EmployeeService();
    private static final DepartmentService departments = new DepartmentService();
    private static final AttendanceService attendance = new AttendanceService();

    public static void main(String[] args) {
        try {
            DBConnection.initSchema();
        } catch (RuntimeException e) {
            System.out.println("Startup failed: " + e.getMessage());
            System.out.println("Check db.properties and make sure the database exists (CREATE DATABASE employee_db;).");
            return;
        }
        System.out.println("=== Employee Management System ===");
        boolean running = true;
        while (running) {
            System.out.println("\n1. Employees\n2. Departments\n3. Attendance\n4. Reports\n0. Exit");
            switch (readInt("Choose: ")) {
                case 1 -> run(Main::employeeMenu);
                case 2 -> run(Main::departmentMenu);
                case 3 -> run(Main::attendanceMenu);
                case 4 -> run(Main::reportMenu);
                case 0 -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        System.out.println("Goodbye!");
    }

    /** Runs a menu and prints friendly messages for expected errors. */
    private static void run(Runnable menu) {
        try {
            menu.run();
        } catch (ValidationException | DataAccessException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Unexpected error: " + e);
        }
    }

    // ---------------- employees
    private static void employeeMenu() {
        System.out.println("\n--- Employees ---\n1. Add\n2. Update\n3. Delete\n4. View one\n5. List all\n"
                + "6. Search by name\n7. Filter by department\n8. Filter by designation\n"
                + "9. Filter by salary range\n10. Assign department\n11. Unassigned employees");
        switch (readInt("Choose: ")) {
            case 1 -> {
                Employee e = new Employee();
                readEmployeeFields(e);
                System.out.println("Added employee with id " + employees.add(e));
            }
            case 2 -> {
                Employee e = employees.get(readInt("Employee id: "));
                System.out.println("Leave blank to keep current value.");
                readEmployeeFields(e);
                employees.update(e);
                System.out.println("Updated.");
            }
            case 3 -> { employees.delete(readInt("Employee id: ")); System.out.println("Deleted."); }
            case 4 -> System.out.println(employees.get(readInt("Employee id: ")));
            case 5 -> printEmployees(employees.list());
            case 6 -> printEmployees(employees.search(readLine("Name contains: ")));
            case 7 -> {
                departments.list().forEach(System.out::println);
                printEmployees(employees.byDepartment(readInt("Department id: ")));
            }
            case 8 -> printEmployees(employees.byDesignation(readLine("Designation contains: ")));
            case 9 -> printEmployees(employees.bySalary(readMoney("Min salary: "), readMoney("Max salary: ")));
            case 10 -> {
                int empId = readInt("Employee id: ");
                departments.list().forEach(System.out::println);
                String d = readLine("Department id (blank to unassign): ");
                employees.assignDepartment(empId, d.isBlank() ? null : Integer.parseInt(d.trim()));
                System.out.println("Department updated.");
            }
            case 11 -> printEmployees(employees.unassigned());
            default -> System.out.println("Invalid option");
        }
    }

    private static void readEmployeeFields(Employee e) {
        boolean edit = e.getEmpId() != 0;
        String v;
        v = readLine("First name" + cur(edit, e.getFirstName()) + ": ");   if (!v.isBlank() || !edit) e.setFirstName(v.trim());
        v = readLine("Last name" + cur(edit, e.getLastName()) + ": ");     if (!v.isBlank() || !edit) e.setLastName(v.trim());
        v = readLine("Email" + cur(edit, e.getEmail()) + ": ");            if (!v.isBlank() || !edit) e.setEmail(v.trim());
        v = readLine("Phone" + cur(edit, e.getPhone()) + ": ");            if (!v.isBlank() || !edit) e.setPhone(v.trim());
        v = readLine("Designation" + cur(edit, e.getDesignation()) + ": "); if (!v.isBlank() || !edit) e.setDesignation(v.trim());
        v = readLine("Salary" + cur(edit, String.valueOf(e.getSalary())) + ": ");
        if (!v.isBlank() || !edit) e.setSalary(parseMoney(v));
        v = readLine("Hire date yyyy-MM-dd" + cur(edit, String.valueOf(e.getHireDate())) + " (blank = today): ");
        if (!v.isBlank()) e.setHireDate(parseDate(v)); else if (!edit) e.setHireDate(LocalDate.now());
        departments.list().forEach(System.out::println);
        v = readLine("Department id" + cur(edit, String.valueOf(e.getDeptId())) + " (blank = none/keep): ");
        if (!v.isBlank()) e.setDeptId(Integer.parseInt(v.trim()));
    }

    private static String cur(boolean edit, String value) { return edit ? " [" + value + "]" : ""; }

    private static void printEmployees(List<Employee> list) {
        if (list.isEmpty()) { System.out.println("No employees found."); return; }
        System.out.printf("%-4s %-22s %-26s %-18s %-12s %-10s %s%n", "ID", "Name", "Email", "Designation", "Salary", "Hired", "Department");
        list.forEach(System.out::println);
        System.out.println(list.size() + " record(s)");
    }

    // ---------------- departments
    private static void departmentMenu() {
        System.out.println("\n--- Departments ---\n1. Add\n2. Update\n3. Delete\n4. List");
        switch (readInt("Choose: ")) {
            case 1 -> System.out.println("Added department with id "
                    + departments.add(readLine("Name: "), readLine("Location: ")));
            case 2 -> {
                int id = readInt("Department id: ");
                departments.update(id, readLine("New name (blank = keep): "), readLine("New location (blank = keep): "));
                System.out.println("Updated.");
            }
            case 3 -> { departments.delete(readInt("Department id: ")); System.out.println("Deleted (employees become unassigned)."); }
            case 4 -> departments.list().forEach(System.out::println);
            default -> System.out.println("Invalid option");
        }
    }

    // ---------------- attendance
    private static void attendanceMenu() {
        System.out.println("\n--- Attendance ---\n1. Mark / update\n2. View by employee\n3. View by date\n"
                + "4. Monthly report for employee\n5. Delete record");
        switch (readInt("Choose: ")) {
            case 1 -> {
                int empId = readInt("Employee id: ");
                String d = readLine("Date yyyy-MM-dd (blank = today): ");
                LocalDate date = d.isBlank() ? LocalDate.now() : parseDate(d);
                String status = readLine("Status (PRESENT/ABSENT/LEAVE/HALF_DAY): ");
                LocalTime cin = null, cout = null;
                if (status.equalsIgnoreCase("PRESENT") || status.equalsIgnoreCase("HALF_DAY")) {
                    cin = readTime("Check-in HH:mm (blank = none): ");
                    cout = readTime("Check-out HH:mm (blank = none): ");
                }
                attendance.mark(empId, date, status, cin, cout);
                System.out.println("Attendance saved.");
            }
            case 2 -> printAttendance(attendance.byEmployee(readInt("Employee id: ")));
            case 3 -> printAttendance(attendance.byDate(parseDate(readLine("Date yyyy-MM-dd: "))));
            case 4 -> {
                int empId = readInt("Employee id: ");
                int year = readInt("Year: ");
                int month = readInt("Month (1-12): ");
                printAttendance(attendance.byMonth(empId, year, month));
                Map<String, Integer> s = attendance.summary(empId, year, month);
                System.out.println("Summary: " + (s.isEmpty() ? "no records" : s));
            }
            case 5 -> { attendance.delete(readInt("Attendance record id: ")); System.out.println("Deleted."); }
            default -> System.out.println("Invalid option");
        }
    }

    private static void printAttendance(List<Attendance> list) {
        if (list.isEmpty()) { System.out.println("No attendance records."); return; }
        list.forEach(System.out::println);
    }

    // ---------------- reports
    private static void reportMenu() {
        System.out.println("\n--- Reports ---\n1. Headcount by department\n2. Today's attendance");
        switch (readInt("Choose: ")) {
            case 1 -> departments.headcount().forEach((k, v) -> System.out.printf("%-22s %d%n", k, v));
            case 2 -> printAttendance(attendance.byDate(LocalDate.now()));
            default -> System.out.println("Invalid option");
        }
    }

    // ---------------- input helpers
    private static String readLine(String prompt) { System.out.print(prompt); return in.nextLine(); }

    private static int readInt(String prompt) {
        while (true) {
            try { return Integer.parseInt(readLine(prompt).trim()); }
            catch (NumberFormatException e) { System.out.println("Please enter a whole number."); }
        }
    }

    private static BigDecimal readMoney(String prompt) {
        while (true) {
            try { return parseMoney(readLine(prompt)); }
            catch (ValidationException e) { System.out.println(e.getMessage()); }
        }
    }

    private static BigDecimal parseMoney(String s) {
        try { return new BigDecimal(s.trim()); }
        catch (NumberFormatException e) { throw new ValidationException("Invalid amount: " + s); }
    }

    private static LocalDate parseDate(String s) {
        try { return LocalDate.parse(s.trim()); }
        catch (RuntimeException e) { throw new ValidationException("Invalid date (use yyyy-MM-dd): " + s); }
    }

    private static LocalTime readTime(String prompt) {
        String s = readLine(prompt);
        if (s.isBlank()) return null;
        try { return LocalTime.parse(s.trim()); }
        catch (RuntimeException e) { throw new ValidationException("Invalid time (use HH:mm): " + s); }
    }
}
