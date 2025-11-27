package mw.magusmis.repository;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import mw.magusmis.database.AppDatabase;
import mw.magusmis.database.dao.EnrollmentDao;
import mw.magusmis.database.dao.StudentDao;
import mw.magusmis.models.Enrollment;
import mw.magusmis.models.Student;

public class StudentRepository {
    private StudentDao studentDao;
    private EnrollmentDao enrollmentDao;

    public StudentRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.studentDao = database.studentDao();
    }

    public void insertStudent(Student student, DatabaseOperationCallback callback) {
        new InsertStudentAsync(studentDao, callback).execute(student);
    }

    public void authenticateStudent(String email, String passwordHash, AuthenticationCallback callback) {
        new AuthenticateStudentAsync(studentDao, callback).execute(email, passwordHash);
    }

    public void checkEmailExists(String email, CheckExistsCallback callback) {
        new CheckEmailExistsAsync(studentDao, callback).execute(email);
    }

    public void getStudentById(String studentId, StudentQueryCallback callback) {
        new GetStudentByIdAsync(studentDao, callback).execute(studentId);
    }

    // Callback interfaces
    public interface DatabaseOperationCallback {
        void onOperationComplete(long result);
        void onOperationFailed(Exception e);
    }

    public interface AuthenticationCallback {
        void onAuthenticationSuccess(Student student);
        void onAuthenticationFailed();
    }

    public interface CheckExistsCallback {
        void onExists(boolean exists);
        void onError(Exception e);
    }

    public interface StudentQueryCallback {
        void onStudentLoaded(Student student);
        void onError(Exception e);
    }

    // Add this interface to StudentRepository
    public interface AcademicInfoCallback {
        void onAcademicInfoLoaded(int courseCount, double gpa, int totalCredits, String recentActivity);
        void onError(Exception e);
    }

    // Add this method to StudentRepository class
    public void getStudentAcademicInfo(String studentId, AcademicInfoCallback callback) {
        new GetAcademicInfoAsync(studentDao, enrollmentDao, callback).execute(studentId);
    }

    // Add this AsyncTask to StudentRepository
    private static class GetAcademicInfoAsync extends AsyncTask<String, Void, GetAcademicInfoAsync.AcademicInfo> {
        private StudentDao studentDao;
        private EnrollmentDao enrollmentDao;
        private AcademicInfoCallback callback;
        private Exception exception;

        GetAcademicInfoAsync(StudentDao studentDao, EnrollmentDao enrollmentDao, AcademicInfoCallback callback) {
            this.studentDao = studentDao;
            this.enrollmentDao = enrollmentDao;
            this.callback = callback;
        }

        @Override
        protected AcademicInfo doInBackground(String... studentIds) {
            try {
                String studentId = studentIds[0];

                // Get enrollments for student
                List<Enrollment> enrollments = enrollmentDao.getEnrollmentsByStudent(studentId);

                int courseCount = enrollments.size();
                double totalGradePoints = 0;
                int totalCredits = 0;
                int gradedCourses = 0;
                StringBuilder recentActivity = new StringBuilder();

                // Calculate GPA and credits
                for (Enrollment enrollment : enrollments) {
                    if (enrollment.getGrade() != null) {
                        double gradePoints = convertGradeToPoints(enrollment.getGrade());
                        totalGradePoints += gradePoints;
                        gradedCourses++;

                        // Add to recent activity
                        if (recentActivity.length() > 0) recentActivity.append("\n");
                        recentActivity.append("• Grade received for ").append(enrollment.getCourseCode())
                                .append(": ").append(enrollment.getGradeLetter());
                    }

                    // Get course credits (simplified - in real app, you'd join with courses table)
                    int credits = getCreditsForCourse(enrollment.getCourseCode());
                    totalCredits += credits;

                    // Add registration activity
                    if ("REGISTERED".equals(enrollment.getStatus())) {
                        if (recentActivity.length() > 0) recentActivity.append("\n");
                        recentActivity.append("• Registered for ").append(enrollment.getCourseCode());
                    }
                }

                double gpa = gradedCourses > 0 ? totalGradePoints / gradedCourses : 0.0;

                // If no recent activity, add a default message
                if (recentActivity.length() == 0) {
                    recentActivity.append("No recent activity");
                }

                return new AcademicInfo(courseCount, gpa, totalCredits, recentActivity.toString());

            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(AcademicInfo academicInfo) {
            if (exception != null) {
                callback.onError(exception);
            } else if (academicInfo != null) {
                callback.onAcademicInfoLoaded(
                        academicInfo.courseCount,
                        academicInfo.gpa,
                        academicInfo.totalCredits,
                        academicInfo.recentActivity
                );
            } else {
                callback.onError(new Exception("Failed to load academic info"));
            }
        }

        private double convertGradeToPoints(double grade) {
            if (grade >= 90) return 4.0;
            if (grade >= 80) return 3.0;
            if (grade >= 70) return 2.0;
            if (grade >= 60) return 1.0;
            return 0.0;
        }

        private int getCreditsForCourse(String courseCode) {
            // Simplified - in real app, you'd query the courses table
            switch (courseCode) {
                case "CS101": return 3;
                case "MATH201": return 4;
                case "PHY301": return 4;
                case "ENG101": return 3;
                default: return 3;
            }
        }

        private static class AcademicInfo {
            int courseCount;
            double gpa;
            int totalCredits;
            String recentActivity;

            AcademicInfo(int courseCount, double gpa, int totalCredits, String recentActivity) {
                this.courseCount = courseCount;
                this.gpa = gpa;
                this.totalCredits = totalCredits;
                this.recentActivity = recentActivity;
            }
        }
    }

    // AsyncTasks for database operations
    private static class InsertStudentAsync extends AsyncTask<Student, Void, Long> {
        private StudentDao studentDao;
        private DatabaseOperationCallback callback;
        private Exception exception;

        InsertStudentAsync(StudentDao studentDao, DatabaseOperationCallback callback) {
            this.studentDao = studentDao;
            this.callback = callback;
        }

        @Override
        protected Long doInBackground(Student... students) {
            try {
                return studentDao.insert(students[0]);
            } catch (Exception e) {
                exception = e;
                return -1L;
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            if (exception != null) {
                callback.onOperationFailed(exception);
            } else if (result > 0) {
                callback.onOperationComplete(result);
            } else {
                callback.onOperationFailed(new Exception("Insert failed"));
            }
        }
    }

    private static class AuthenticateStudentAsync extends AsyncTask<String, Void, Student> {
        private StudentDao studentDao;
        private AuthenticationCallback callback;

        AuthenticateStudentAsync(StudentDao studentDao, AuthenticationCallback callback) {
            this.studentDao = studentDao;
            this.callback = callback;
        }

        @Override
        protected Student doInBackground(String... credentials) {
            return studentDao.login(credentials[0], credentials[1]);
        }

        @Override
        protected void onPostExecute(Student student) {
            if (student != null) {
                callback.onAuthenticationSuccess(student);
            } else {
                callback.onAuthenticationFailed();
            }
        }
    }

    private static class CheckEmailExistsAsync extends AsyncTask<String, Void, Boolean> {
        private StudentDao studentDao;
        private CheckExistsCallback callback;
        private Exception exception;

        CheckEmailExistsAsync(StudentDao studentDao, CheckExistsCallback callback) {
            this.studentDao = studentDao;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... emails) {
            try {
                int count = studentDao.checkEmailExists(emails[0]);
                return count > 0;
            } catch (Exception e) {
                exception = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean exists) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onExists(exists);
            }
        }
    }

    private static class GetStudentByIdAsync extends AsyncTask<String, Void, Student> {
        private StudentDao studentDao;
        private StudentQueryCallback callback;
        private Exception exception;

        GetStudentByIdAsync(StudentDao studentDao, StudentQueryCallback callback) {
            this.studentDao = studentDao;
            this.callback = callback;
        }

        @Override
        protected Student doInBackground(String... studentIds) {
            try {
                return studentDao.getStudentById(studentIds[0]);
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Student student) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onStudentLoaded(student);
            }
        }
    }
}