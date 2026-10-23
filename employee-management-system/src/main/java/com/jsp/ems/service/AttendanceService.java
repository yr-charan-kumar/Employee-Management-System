package com.jsp.ems.service;

import com.jsp.ems.dao.AttendanceDAO;
import com.jsp.ems.dao.EmployeeDAO;
import com.jsp.ems.dao.impl.AttendanceDAOImpl;
import com.jsp.ems.dao.impl.EmployeeDAOImpl;
import com.jsp.ems.exception.ValidationException;
import com.jsp.ems.model.Attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttendanceService {

    private static final Set<String> STATUSES = Set.of("PRESENT", "ABSENT", "LEAVE", "HALF_DAY");

    private final AttendanceDAO dao = new AttendanceDAOImpl();
    private final EmployeeDAO empDao = new EmployeeDAOImpl();

    public void mark(int empId, LocalDate date, String status, LocalTime in, LocalTime out) {
        if (empDao.findById(empId).isEmpty()) throw new ValidationException("Employee " + empId + " not found");
        String s = status == null ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(s)) throw new ValidationException("Status must be one of " + STATUSES);
        if (in != null && out != null && out.isBefore(in)) throw new ValidationException("Check-out is before check-in");
        Attendance a = new Attendance();
        a.setEmpId(empId);
        a.setDate(date);
        a.setStatus(s);
        a.setCheckIn(in);
        a.setCheckOut(out);
        dao.mark(a);
    }

    public void delete(int attId) {
        if (!dao.delete(attId)) throw new ValidationException("Attendance record " + attId + " not found");
    }

    public List<Attendance> byEmployee(int empId) { return dao.findByEmployee(empId); }

    public List<Attendance> byDate(LocalDate date) { return dao.findByDate(date); }

    public List<Attendance> byMonth(int empId, int year, int month) { return dao.findByEmployeeAndMonth(empId, year, month); }

    public Map<String, Integer> summary(int empId, int year, int month) { return dao.monthlySummary(empId, year, month); }
}
