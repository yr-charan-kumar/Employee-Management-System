package com.jsp.ems.dao.impl;

import com.jsp.ems.dao.AttendanceDAO;
import com.jsp.ems.exception.DataAccessException;
import com.jsp.ems.model.Attendance;
import com.jsp.ems.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class AttendanceDAOImpl implements AttendanceDAO {

    private static final String SELECT_BASE =
            "SELECT a.att_id, a.emp_id, e.first_name || ' ' || e.last_name AS emp_name, "
          + "a.att_date, a.status, a.check_in, a.check_out "
          + "FROM attendance a JOIN employees e ON a.emp_id = e.emp_id ";

    private Attendance map(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setAttId(rs.getInt("att_id"));
        a.setEmpId(rs.getInt("emp_id"));
        a.setEmpName(rs.getString("emp_name"));
        a.setDate(rs.getObject("att_date", LocalDate.class));
        a.setStatus(rs.getString("status"));
        a.setCheckIn(rs.getObject("check_in", LocalTime.class));
        a.setCheckOut(rs.getObject("check_out", LocalTime.class));
        return a;
    }

    private List<Attendance> query(String where, Object... params) {
        List<Attendance> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_BASE + where)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Attendance query failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void mark(Attendance a) {
        String sql = "INSERT INTO attendance (emp_id, att_date, status, check_in, check_out) VALUES (?, ?, ?, ?, ?) "
                + "ON CONFLICT (emp_id, att_date) DO UPDATE SET status = EXCLUDED.status, "
                + "check_in = EXCLUDED.check_in, check_out = EXCLUDED.check_out";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, a.getEmpId());
            ps.setObject(2, a.getDate());
            ps.setString(3, a.getStatus());
            ps.setObject(4, a.getCheckIn(), Types.TIME);
            ps.setObject(5, a.getCheckOut(), Types.TIME);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not mark attendance: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int attId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM attendance WHERE att_id = ?")) {
            ps.setInt(1, attId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete attendance: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Attendance> findByEmployee(int empId) {
        return query("WHERE a.emp_id = ? ORDER BY a.att_date DESC", empId);
    }

    @Override
    public List<Attendance> findByDate(LocalDate date) {
        return query("WHERE a.att_date = ? ORDER BY e.first_name", date);
    }

    @Override
    public List<Attendance> findByEmployeeAndMonth(int empId, int year, int month) {
        return query("WHERE a.emp_id = ? AND EXTRACT(YEAR FROM a.att_date) = ? "
                + "AND EXTRACT(MONTH FROM a.att_date) = ? ORDER BY a.att_date", empId, year, month);
    }

    @Override
    public Map<String, Integer> monthlySummary(int empId, int year, int month) {
        String sql = "SELECT status, COUNT(*) AS days FROM attendance WHERE emp_id = ? "
                + "AND EXTRACT(YEAR FROM att_date) = ? AND EXTRACT(MONTH FROM att_date) = ? GROUP BY status";
        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.put(rs.getString("status"), rs.getInt("days"));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Could not compute summary: " + e.getMessage(), e);
        }
    }
}
