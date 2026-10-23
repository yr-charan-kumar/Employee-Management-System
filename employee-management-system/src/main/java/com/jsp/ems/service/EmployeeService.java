package com.jsp.ems.service;

import com.jsp.ems.dao.DepartmentDAO;
import com.jsp.ems.dao.EmployeeDAO;
import com.jsp.ems.dao.impl.DepartmentDAOImpl;
import com.jsp.ems.dao.impl.EmployeeDAOImpl;
import com.jsp.ems.exception.ValidationException;
import com.jsp.ems.model.Employee;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

public class EmployeeService {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final EmployeeDAO dao = new EmployeeDAOImpl();
    private final DepartmentDAO deptDao = new DepartmentDAOImpl();

    private void validate(Employee e) {
        if (e.getFirstName() == null || e.getFirstName().isBlank()) throw new ValidationException("First name is required");
        if (e.getLastName() == null || e.getLastName().isBlank()) throw new ValidationException("Last name is required");
        if (e.getEmail() == null || !EMAIL.matcher(e.getEmail()).matches()) throw new ValidationException("A valid email is required");
        if (e.getSalary() == null || e.getSalary().signum() < 0) throw new ValidationException("Salary must be zero or more");
        if (e.getHireDate() == null) throw new ValidationException("Hire date is required");
        if (e.getDeptId() != null && deptDao.findById(e.getDeptId()).isEmpty())
            throw new ValidationException("Department " + e.getDeptId() + " does not exist");
    }

    public int add(Employee e) { validate(e); return dao.add(e); }

    public void update(Employee e) {
        validate(e);
        if (!dao.update(e)) throw new ValidationException("Employee " + e.getEmpId() + " not found");
    }

    public void delete(int id) {
        if (!dao.delete(id)) throw new ValidationException("Employee " + id + " not found");
    }

    public Employee get(int id) {
        return dao.findById(id).orElseThrow(() -> new ValidationException("Employee " + id + " not found"));
    }

    public List<Employee> list() { return dao.findAll(); }

    public List<Employee> search(String keyword) { return dao.searchByName(keyword); }

    public List<Employee> byDepartment(int deptId) { return dao.filterByDepartment(deptId); }

    public List<Employee> byDesignation(String d) { return dao.filterByDesignation(d); }

    public List<Employee> bySalary(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) > 0) throw new ValidationException("Minimum salary exceeds maximum");
        return dao.filterBySalaryRange(min, max);
    }

    public List<Employee> unassigned() { return dao.findUnassigned(); }

    public void assignDepartment(int empId, Integer deptId) {
        get(empId);
        if (deptId != null && deptDao.findById(deptId).isEmpty())
            throw new ValidationException("Department " + deptId + " does not exist");
        dao.assignDepartment(empId, deptId);
    }
}
