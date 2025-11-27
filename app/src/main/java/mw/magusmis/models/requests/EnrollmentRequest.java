package mw.magusmis.models.requests;

public class EnrollmentRequest {
    private String studentId;
    private String courseCode;
    private String status;

    public EnrollmentRequest() {
    }

    public EnrollmentRequest(String studentId, String courseCode, String status) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
