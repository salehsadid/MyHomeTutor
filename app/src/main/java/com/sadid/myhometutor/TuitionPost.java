package com.sadid.myhometutor;

public class TuitionPost {
    private String id;
    private String studentId;
    private String subject;
    private String grade; // This corresponds to "class" in Firestore
    private String salary;
    private String location; // This might be constructed from division, district, etc.
    private String status;

    // New fields
    private String tuitionType;
    private String group;
    private String preferredGender;
    private String medium;
    private String daysPerWeek;
    private String hoursPerDay;
    private String preferredTiming;
    private String division;
    private String district;
    private String thana;
    private String area;
    private String detailedAddress;
    private String additionalReq;
    private boolean isUrgent;
    private long timestamp;

    public TuitionPost() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; } // In Firestore it is "class"
    public void setClass(String grade) { this.grade = grade; } // For Firestore mapping if needed
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTuitionType() { return tuitionType; }
    public void setTuitionType(String tuitionType) { this.tuitionType = tuitionType; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    public String getPreferredGender() { return preferredGender; }
    public void setPreferredGender(String preferredGender) { this.preferredGender = preferredGender; }
    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }
    public String getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(String daysPerWeek) { this.daysPerWeek = daysPerWeek; }
    public String getHoursPerDay() { return hoursPerDay; }
    public void setHoursPerDay(String hoursPerDay) { this.hoursPerDay = hoursPerDay; }
    public String getPreferredTiming() { return preferredTiming; }
    public void setPreferredTiming(String preferredTiming) { this.preferredTiming = preferredTiming; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getThana() { return thana; }
    public void setThana(String thana) { this.thana = thana; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getDetailedAddress() { return detailedAddress; }
    public void setDetailedAddress(String detailedAddress) { this.detailedAddress = detailedAddress; }
    public String getAdditionalReq() { return additionalReq; }
    public void setAdditionalReq(String additionalReq) { this.additionalReq = additionalReq; }
    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
