package mw.magusmis.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "enrollments")
public class Enrollment implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int enrollmentId;

    @NonNull
    private String studentId;

    @NonNull
    private String courseCode;

    private String enrollmentDate;
    private String status; // "REGISTERED", "COMPLETED", "DROPPED"
    private Double grade;

    public Enrollment() {
        // Initialize required fields
        this.studentId = "";
        this.courseCode = "";
        this.status = "REGISTERED";
    }

    public Enrollment(@NonNull String studentId, @NonNull String courseCode, String status) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.status = status;
        this.enrollmentDate = java.time.LocalDate.now().toString();
    }

    // Getters and Setters
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    @NonNull
    public String getStudentId() { return studentId; }
    public void setStudentId(@NonNull String studentId) { this.studentId = studentId; }

    @NonNull
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(@NonNull String courseCode) { this.courseCode = courseCode; }

    public String getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(String enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }

    public String getGradeLetter() {
        if (grade == null) return "N/A";
        if (grade >= 90) return "A";
        if (grade >= 80) return "B";
        if (grade >= 70) return "C";
        if (grade >= 60) return "D";
        return "F";
    }
}