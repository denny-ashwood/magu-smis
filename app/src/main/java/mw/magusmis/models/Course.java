package mw.magusmis.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "courses")
public class Course implements Serializable {
    @PrimaryKey
    @NonNull
    private String courseCode;

    @NonNull
    private String courseName;

    private String description;
    private int credits;
    private String department;
    private String instructor;
    private String schedule;
    private int capacity;
    private int enrolledStudents;
    private String semester;

    public Course() {
        // Initialize required fields
        this.courseCode = "";
        this.courseName = "";
        this.capacity = 0;
        this.enrolledStudents = 0;
        this.credits = 0;
    }

    public Course(@NonNull String courseCode, @NonNull String courseName, String description,
                  int credits, String department, String instructor,
                  String schedule, int capacity, int enrolledStudents, String semester) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.credits = credits;
        this.department = department;
        this.instructor = instructor;
        this.schedule = schedule;
        this.capacity = capacity;
        this.enrolledStudents = enrolledStudents;
        this.semester = semester;
    }

    // Getters and Setters
    @NonNull
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(@NonNull String courseCode) { this.courseCode = courseCode; }

    @NonNull
    public String getCourseName() { return courseName; }
    public void setCourseName(@NonNull String courseName) { this.courseName = courseName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(int enrolledStudents) { this.enrolledStudents = enrolledStudents; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public boolean hasAvailableSeats() {
        return enrolledStudents < capacity;
    }

    public int getAvailableSeats() {
        return capacity - enrolledStudents;
    }

    @Ignore
    private boolean isRegistered = false;

    @Ignore
    private boolean isLoading = false;

    // Add getters and setters
    public boolean isRegistered() { return isRegistered; }
    public void setRegistered(boolean registered) { isRegistered = registered; }

    public boolean isLoading() { return isLoading; }
    public void setLoading(boolean loading) { isLoading = loading; }
}