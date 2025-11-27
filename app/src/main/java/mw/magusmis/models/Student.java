package mw.magusmis.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "students")
public class Student implements Serializable {
    @PrimaryKey
    @NonNull
    private String studentId;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    private String email;

    @NonNull
    private String passwordHash;

    private String phoneNumber;
    private String department;
    private String enrollmentDate;
    private String profileImageUrl;

    // Default constructor for Room
    public Student() {
        this.studentId = "";
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.passwordHash = "";
    }

    // Convenience constructor for creating new students
    @Ignore
    public Student(@NonNull String studentId, @NonNull String firstName, @NonNull String lastName,
                   @NonNull String email, @NonNull String passwordHash, String department) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.department = department;
        this.enrollmentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Getters and Setters (keep your existing ones)
    @NonNull
    public String getStudentId() { return studentId; }
    public void setStudentId(@NonNull String studentId) { this.studentId = studentId; }

    @NonNull
    public String getFirstName() { return firstName; }
    public void setFirstName(@NonNull String firstName) { this.firstName = firstName; }

    @NonNull
    public String getLastName() { return lastName; }
    public void setLastName(@NonNull String lastName) { this.lastName = lastName; }

    @NonNull
    public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }

    @NonNull
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(@NonNull String passwordHash) { this.passwordHash = passwordHash; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(String enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    @Ignore
    public String getFullName() {
        return firstName + " " + lastName;
    }
}