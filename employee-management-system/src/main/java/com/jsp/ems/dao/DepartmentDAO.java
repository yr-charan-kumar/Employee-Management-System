package com.jsp.ems.dao;

import com.jsp.ems.model.Department;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DepartmentDAO {
    int add(Department d);
    boolean update(Department d);
    boolean delete(int deptId);
    Optional<Department> findById(int deptId);
    List<Department> findAll();
    /** Department name -> number of employees (includes empty departments). */
    Map<String, Integer> headcount();
}
