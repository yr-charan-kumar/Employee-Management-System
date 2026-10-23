package com.jsp.ems.service;

import com.jsp.ems.dao.DepartmentDAO;
import com.jsp.ems.dao.impl.DepartmentDAOImpl;
import com.jsp.ems.exception.ValidationException;
import com.jsp.ems.model.Department;

import java.util.List;
import java.util.Map;

public class DepartmentService {

    private final DepartmentDAO dao = new DepartmentDAOImpl();

    public int add(String name, String location) {
        if (name == null || name.isBlank()) throw new ValidationException("Department name is required");
        return dao.add(new Department(name.trim(), location));
    }

    public void update(int id, String name, String location) {
        Department d = get(id);
        if (name != null && !name.isBlank()) d.setDeptName(name.trim());
        if (location != null && !location.isBlank()) d.setLocation(location.trim());
        dao.update(d);
    }

    public void delete(int id) {
        if (!dao.delete(id)) throw new ValidationException("Department " + id + " not found");
    }

    public Department get(int id) {
        return dao.findById(id).orElseThrow(() -> new ValidationException("Department " + id + " not found"));
    }

    public List<Department> list() { return dao.findAll(); }

    public Map<String, Integer> headcount() { return dao.headcount(); }
}
