package com.sadid.myhometutor;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
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
    
    @Exclude
    public Date getTimestamp() { return timestamp; }
    
    @Exclude
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    // Custom getter/setter to handle both Long and Timestamp from Firestore
    @PropertyName("timestamp")
    public Object getTimestampForFirestore() {
        return timestamp;
    }
    
    @PropertyName("timestamp")
    public void setTimestampFromFirestore(Object value) {
        if (value instanceof Timestamp) {
            this.timestamp = ((Timestamp) value).toDate();
        } else if (value instanceof Long) {
            this.timestamp = new Date((Long) value);
        } else if (value instanceof Date) {
            this.timestamp = (Date) value;
        } else {
            this.timestamp = null;
        }
    }
    
    @Exclude
    public TuitionPost getTuitionPost() { return tuitionPost; }
    
    @Exclude
    public void setTuitionPost(TuitionPost tuitionPost) { this.tuitionPost = tuitionPost; }
}

