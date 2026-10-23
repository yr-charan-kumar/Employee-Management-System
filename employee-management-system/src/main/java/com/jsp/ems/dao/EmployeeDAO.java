package com.jsp.ems.dao;

import com.jsp.ems.model.Employee;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EmployeeDAO {
    int add(Employee e);
    boolean update(Employee e);
    boolean delete(int empId);
    Optional<Employee> findById(int empId);
    List<Employee> findAll();

    // search / filter
    List<Employee> searchByName(String keyword);
    List<Employee> filterByDepartment(int deptId);
    List<Employee> filterByDesignation(String designation);
    List<Employee> filterBySalaryRange(BigDecimal min, BigDecimal max);
    List<Employee> findUnassigned();

    boolean assignDepartment(int empId, Integer deptId);
}
