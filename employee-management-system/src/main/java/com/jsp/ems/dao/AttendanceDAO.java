package com.jsp.ems.dao;

import com.jsp.ems.model.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AttendanceDAO {
    /** Inserts or updates the record for (employee, date). */
    void mark(Attendance a);
    boolean delete(int attId);
    List<Attendance> findByEmployee(int empId);
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByEmployeeAndMonth(int empId, int year, int month);
    /** status -> days, for one employee in one month. */
    Map<String, Integer> monthlySummary(int empId, int year, int month);
}
