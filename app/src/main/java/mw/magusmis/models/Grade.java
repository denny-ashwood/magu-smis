// Grade.java
package mw.magusmis.models;

import java.time.LocalDateTime;

public class Grade {
    private int gradeId;
    private String studentId;
    private String courseId;
    private String semester;
    private double gradeValue;
    private String gradeLetter;
    private int creditsEarned;
    private LocalDateTime recordedDate;

    // Constructors, getters, setters
    public String calculateGradeLetter() {
        if (gradeValue >= 90) return "A";
        else if (gradeValue >= 80) return "B";
        else if (gradeValue >= 70) return "C";
        else if (gradeValue >= 60) return "D";
        else return "F";
    }
}