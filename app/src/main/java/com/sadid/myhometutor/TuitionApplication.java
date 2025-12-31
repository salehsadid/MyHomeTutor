package com.sadid.myhometutor;

public class TuitionApplication {
    private String applicationId;
    private String tutorId;
    private String postId;
    private String studentId;
    private String status;
    private long timestamp;
    private TuitionPost tuitionPost;

    public TuitionApplication() {}

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public TuitionPost getTuitionPost() { return tuitionPost; }
    public void setTuitionPost(TuitionPost tuitionPost) { this.tuitionPost = tuitionPost; }
}

