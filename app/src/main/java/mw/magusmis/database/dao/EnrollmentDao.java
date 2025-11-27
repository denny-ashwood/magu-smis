package mw.magusmis.database.dao;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import mw.magusmis.models.Enrollment;
import java.util.List;

@Dao
public interface EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Enrollment enrollment);

    @Update
    int update(Enrollment enrollment);

    @Query("SELECT * FROM enrollments WHERE studentId = :studentId")
    List<Enrollment> getEnrollmentsByStudent(String studentId);

    @Query("SELECT * FROM enrollments WHERE studentId = :studentId AND courseCode = :courseCode")
    Enrollment getEnrollment(String studentId, String courseCode);

    @Query("SELECT * FROM enrollments WHERE studentId = :studentId AND status = :status")
    List<Enrollment> getEnrollmentsByStatus(String studentId, String status);

    @Query("SELECT COUNT(*) FROM enrollments WHERE studentId = :studentId AND courseCode = :courseCode AND status = 'REGISTERED'")
    int isStudentEnrolled(String studentId, String courseCode);

    @Query("UPDATE enrollments SET grade = :grade WHERE enrollmentId = :enrollmentId")
    int updateGrade(int enrollmentId, double grade);

    @Query("UPDATE enrollments SET status = :status WHERE enrollmentId = :enrollmentId")
    int updateStatus(int enrollmentId, String status);

    //@Query("SELECT e.* FROM enrollments e JOIN courses c ON e.courseCode = c.courseCode WHERE e.studentId = :studentId")
    //List<Enrollment> getEnrollmentsWithCourseDetails(String studentId);

    @Query("DELETE FROM enrollments")
    void deleteAllEnrollments();

    @Query("SELECT COUNT(*) FROM enrollments WHERE studentId = :studentId")
    int getEnrollmentCountByStudent(String studentId);

    @Query("SELECT AVG(grade) FROM enrollments WHERE studentId = :studentId AND grade IS NOT NULL")
    Double getAverageGradeByStudent(String studentId);

    @Query("SELECT e.*, c.courseName, c.credits FROM enrollments e JOIN courses c ON e.courseCode = c.courseCode WHERE e.studentId = :studentId")
    List<EnrollmentWithCourse> getEnrollmentsWithCourseDetails(String studentId);

    @Query("SELECT COUNT(*) FROM enrollments WHERE studentId = :studentId AND courseCode = :courseCode AND status IN ('REGISTERED', 'COMPLETED')")
    int isStudentAlreadyEnrolled(String studentId, String courseCode);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertEnrollment(Enrollment enrollment);

    // Add this class for joined queries
    class EnrollmentWithCourse {
        @Embedded
        public Enrollment enrollment;

        @ColumnInfo(name = "courseName")
        public String courseName;

        @ColumnInfo(name = "credits")
        public int credits;
    }
}