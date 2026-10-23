package com.jsp.ems.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Attendance {
    private int attId;
    private int empId;
    private String empName;
    private LocalDate date;
    private String status;       // PRESENT, ABSENT, LEAVE, HALF_DAY
    private LocalTime checkIn;
    private LocalTime checkOut;

    public int getAttId() { return attId; }
    public void setAttId(int attId) { this.attId = attId; }
    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }
    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalTime checkIn) { this.checkIn = checkIn; }
    public LocalTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalTime checkOut) { this.checkOut = checkOut; }

    @Override
    public String toString() {
        return String.format("%-4d %-22s %-12s %-10s in=%-9s out=%s",
                empId, empName == null ? "-" : empName, date, status,
                checkIn == null ? "-" : checkIn, checkOut == null ? "-" : checkOut);
    }
}
