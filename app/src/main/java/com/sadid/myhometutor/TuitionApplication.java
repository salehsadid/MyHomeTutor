package com.sadid.myhometutor;

import com.google.firebase.firestore.Exclude;
import java.util.Date;

public class TuitionApplication {
    private String applicationId;
    private String tutorId;
    private String postId;
    private String studentId;
    private String status;
    private Date timestamp;
    private TuitionPost tuitionPost;
    
    // Transient fields for Admin Search
    private String studentName;
    private String tutorName;
    private String tutorUniversity;

    public TuitionApplication() {}

    @Exclude public String getStudentName() { return studentName; }
    @Exclude public void setStudentName(String studentName) { this.studentName = studentName; }
    
    @Exclude public String getTutorName() { return tutorName; }
    @Exclude public void setTutorName(String tutorName) { this.tutorName = tutorName; }
    
    @Exclude public String getTutorUniversity() { return tutorUniversity; }
    @Exclude public void setTutorUniversity(String tutorUniversity) { this.tutorUniversity = tutorUniversity; }

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
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public TuitionPost getTuitionPost() { return tuitionPost; }
    public void setTuitionPost(TuitionPost tuitionPost) { this.tuitionPost = tuitionPost; }
}

