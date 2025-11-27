package mw.magusmis.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import mw.magusmis.models.Course;
import java.util.List;

@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Course course);

    @Update
    int update(Course course);

    @Query("SELECT * FROM courses")
    List<Course> getAllCourses();

    @Query("SELECT * FROM courses WHERE courseCode = :courseCode")
    Course getCourseByCode(String courseCode);

    @Query("SELECT * FROM courses WHERE department = :department")
    List<Course> getCoursesByDepartment(String department);

    @Query("SELECT * FROM courses WHERE semester = :semester")
    List<Course> getCoursesBySemester(String semester);

    @Query("SELECT * FROM courses WHERE courseName LIKE '%' || :query || '%' OR courseCode LIKE '%' || :query || '%'")
    List<Course> searchCourses(String query);

    @Query("UPDATE courses SET enrolledStudents = enrolledStudents + 1 WHERE courseCode = :courseCode AND enrolledStudents < capacity")
    int incrementEnrolledStudents(String courseCode);

    //@Query("UPDATE courses SET enrolledStudents = enrolledStudents - 1 WHERE courseCode = :courseCode AND enrolledStudents > 0")
    //int decrementEnrolledStudents(String courseCode);

    @Query("DELETE FROM courses")
    void deleteAllCourses();

    @Query("SELECT * FROM courses WHERE capacity > enrolledStudents")
    List<Course> getAvailableCourses();

    @Query("SELECT * FROM courses WHERE courseCode = :courseCode AND capacity > enrolledStudents")
    Course getAvailableCourseByCode(String courseCode);

    @Query("UPDATE courses SET enrolledStudents = enrolledStudents + 1 WHERE courseCode = :courseCode AND enrolledStudents < capacity")
    int enrollStudentInCourse(String courseCode);

    @Query("SELECT COUNT(*) FROM courses WHERE courseCode = :courseCode AND capacity > enrolledStudents")
    int isCourseAvailable(String courseCode);

    @Query("UPDATE courses SET enrolledStudents = enrolledStudents - 1 WHERE courseCode = :courseCode AND enrolledStudents > 0")
    int decrementEnrolledStudents(String courseCode);
}