package com.jsp.ems.dao.impl;

import com.jsp.ems.dao.DepartmentDAO;
import com.jsp.ems.exception.DataAccessException;
import com.jsp.ems.model.Department;
import com.jsp.ems.util.DBConnection;

import java.sql.*;
import java.util.*;

public class DepartmentDAOImpl implements DepartmentDAO {

    private Department map(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setDeptId(rs.getInt("dept_id"));
        d.setDeptName(rs.getString("dept_name"));
        d.setLocation(rs.getString("location"));
        return d;
    }

    @Override
    public int add(Department d) {
        String sql = "INSERT INTO departments (dept_name, location) VALUES (?, ?) RETURNING dept_id";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getDeptName());
            ps.setString(2, d.getLocation());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                d.setDeptId(rs.getInt(1));
                return d.getDeptId();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not add department: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Department d) {
        String sql = "UPDATE departments SET dept_name = ?, location = ? WHERE dept_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getDeptName());
            ps.setString(2, d.getLocation());
            ps.setInt(3, d.getDeptId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update department: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int deptId) {
        String sql = "DELETE FROM departments WHERE dept_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete department: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Department> findById(int deptId) {
        String sql = "SELECT * FROM departments WHERE dept_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not fetch department: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Department> findAll() {
        List<Department> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM departments ORDER BY dept_id")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not list departments: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> headcount() {
        String sql = "SELECT d.dept_name, COUNT(e.emp_id) AS total FROM departments d "
                + "LEFT JOIN employees e ON e.dept_id = d.dept_id "
                + "GROUP BY d.dept_name ORDER BY total DESC, d.dept_name";
        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) result.put(rs.getString("dept_name"), rs.getInt("total"));
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Could not compute headcount: " + e.getMessage(), e);
        }
    }
}
