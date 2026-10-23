package com.jsp.ems.dao.impl;

import com.jsp.ems.dao.EmployeeDAO;
import com.jsp.ems.exception.DataAccessException;
import com.jsp.ems.model.Employee;
import com.jsp.ems.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAOImpl implements EmployeeDAO {

    private static final String SELECT_BASE =
            "SELECT e.emp_id, e.first_name, e.last_name, e.email, e.phone, e.designation, "
          + "e.salary, e.hire_date, e.dept_id, d.dept_name "
          + "FROM employees e LEFT JOIN departments d ON e.dept_id = d.dept_id ";

    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmpId(rs.getInt("emp_id"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setDesignation(rs.getString("designation"));
        e.setSalary(rs.getBigDecimal("salary"));
        e.setHireDate(rs.getObject("hire_date", java.time.LocalDate.class));
        int dept = rs.getInt("dept_id");
        e.setDeptId(rs.wasNull() ? null : dept);
        e.setDeptName(rs.getString("dept_name"));
        return e;
    }

    private void setDept(PreparedStatement ps, int idx, Integer deptId) throws SQLException {
        if (deptId == null) ps.setNull(idx, Types.INTEGER); else ps.setInt(idx, deptId);
    }

    /** Runs a SELECT with the given parameters and maps every row. */
    private List<Employee> query(String where, Object... params) {
        String sql = SELECT_BASE + where;
        List<Employee> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new DataAccessException("Employee query failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public int add(Employee e) {
        String sql = "INSERT INTO employees (first_name, last_name, email, phone, designation, salary, hire_date, dept_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING emp_id";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getFirstName());
            ps.setString(2, e.getLastName());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getPhone());
            ps.setString(5, e.getDesignation());
            ps.setBigDecimal(6, e.getSalary());
            ps.setObject(7, e.getHireDate());
            setDept(ps, 8, e.getDeptId());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                e.setEmpId(rs.getInt(1));
                return e.getEmpId();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not add employee: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean update(Employee e) {
        String sql = "UPDATE employees SET first_name=?, last_name=?, email=?, phone=?, designation=?, "
                + "salary=?, hire_date=?, dept_id=? WHERE emp_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getFirstName());
            ps.setString(2, e.getLastName());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getPhone());
            ps.setString(5, e.getDesignation());
            ps.setBigDecimal(6, e.getSalary());
            ps.setObject(7, e.getHireDate());
            setDept(ps, 8, e.getDeptId());
            ps.setInt(9, e.getEmpId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update employee: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean delete(int empId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM employees WHERE emp_id = ?")) {
            ps.setInt(1, empId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete employee: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<Employee> findById(int empId) {
        return query("WHERE e.emp_id = ?", empId).stream().findFirst();
    }

    @Override
    public List<Employee> findAll() {
        return query("ORDER BY e.emp_id");
    }

    @Override
    public List<Employee> searchByName(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        return query("WHERE LOWER(e.first_name) LIKE ? OR LOWER(e.last_name) LIKE ? "
                + "OR LOWER(e.first_name || ' ' || e.last_name) LIKE ? ORDER BY e.first_name", like, like, like);
    }

    @Override
    public List<Employee> filterByDepartment(int deptId) {
        return query("WHERE e.dept_id = ? ORDER BY e.first_name", deptId);
    }

    @Override
    public List<Employee> filterByDesignation(String designation) {
        return query("WHERE LOWER(e.designation) LIKE ? ORDER BY e.first_name",
                "%" + designation.toLowerCase() + "%");
    }

    @Override
    public List<Employee> filterBySalaryRange(BigDecimal min, BigDecimal max) {
        return query("WHERE e.salary BETWEEN ? AND ? ORDER BY e.salary DESC", min, max);
    }

    @Override
    public List<Employee> findUnassigned() {
        return query("WHERE e.dept_id IS NULL ORDER BY e.first_name");
    }

    @Override
    public boolean assignDepartment(int empId, Integer deptId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE employees SET dept_id = ? WHERE emp_id = ?")) {
            setDept(ps, 1, deptId);
            ps.setInt(2, empId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not assign department: " + ex.getMessage(), ex);
        }
    }
}
