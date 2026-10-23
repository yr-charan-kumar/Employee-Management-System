package com.jsp.ems.model;

public class Department {
    private int deptId;
    private String deptName;
    private String location;

    public Department() { }
    public Department(String deptName, String location) { this.deptName = deptName; this.location = location; }

    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return String.format("%-4d %-22s %s", deptId, deptName, location == null ? "-" : location);
    }
}
