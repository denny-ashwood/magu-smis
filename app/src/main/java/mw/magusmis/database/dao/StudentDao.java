package mw.magusmis.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import mw.magusmis.models.Student;

@Dao
public interface StudentDao {
    @Insert
    long insert(Student student);

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    Student getStudentById(String studentId);

    @Query("SELECT * FROM students WHERE email = :email")
    Student getStudentByEmail(String email);

    @Query("SELECT * FROM students WHERE email = :email AND passwordHash = :passwordHash")
    Student login(String email, String passwordHash);

    @Query("SELECT COUNT(*) FROM students WHERE email = :email")
    int checkEmailExists(String email);

    @Query("SELECT COUNT(*) FROM students WHERE studentId = :studentId")
    int checkStudentIdExists(String studentId);

    @Query("DELETE FROM students")
    void deleteAllStudents();
}