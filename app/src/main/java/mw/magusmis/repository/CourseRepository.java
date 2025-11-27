package mw.magusmis.repository;

import android.content.Context;
import android.os.AsyncTask;

import mw.magusmis.database.AppDatabase;
import mw.magusmis.database.dao.CourseDao;
import mw.magusmis.database.dao.EnrollmentDao;
import mw.magusmis.models.Course;
import mw.magusmis.models.Enrollment;

import java.util.List;

public class CourseRepository {
    private CourseDao courseDao;
    private EnrollmentDao enrollmentDao;

    public CourseRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.courseDao = database.courseDao();
        this.enrollmentDao = database.enrollmentDao();
    }

    // Get all available courses (with seats available)
    public void getAvailableCourses(CoursesCallback callback) {
        new GetAvailableCoursesAsync(courseDao, callback).execute();
    }

    // Get all courses (including full ones)
    public void getAllCourses(CoursesCallback callback) {
        new GetAllCoursesAsync(courseDao, callback).execute();
    }

    // Register student for a course
    public void registerForCourse(String studentId, String courseCode, RegistrationCallback callback) {
        new RegisterForCourseAsync(courseDao, enrollmentDao, callback).execute(studentId, courseCode);
    }

    // Get student's enrolled courses
    public void getStudentEnrollments(String studentId, EnrollmentsCallback callback) {
        new GetStudentEnrollmentsAsync(enrollmentDao, callback).execute(studentId);
    }

    // Drop a course
    public void dropCourse(int enrollmentId, String courseCode, RegistrationCallback callback) {
        new DropCourseAsync(courseDao, enrollmentDao, callback).execute(
                String.valueOf(enrollmentId), courseCode
        );
    }

    // Callback interfaces
    public interface CoursesCallback {
        void onCoursesLoaded(List<Course> courses);
        void onError(Exception e);
    }

    public interface RegistrationCallback {
        void onRegistrationSuccess();
        void onRegistrationFailed(String error);
    }

    public interface EnrollmentsCallback {
        void onEnrollmentsLoaded(List<Enrollment> enrollments);
        void onError(Exception e);
    }

    // AsyncTasks
    private static class GetAvailableCoursesAsync extends AsyncTask<Void, Void, List<Course>> {
        private CourseDao courseDao;
        private CoursesCallback callback;
        private Exception exception;

        GetAvailableCoursesAsync(CourseDao courseDao, CoursesCallback callback) {
            this.courseDao = courseDao;
            this.callback = callback;
        }

        @Override
        protected List<Course> doInBackground(Void... voids) {
            try {
                return courseDao.getAvailableCourses();
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Course> courses) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onCoursesLoaded(courses);
            }
        }
    }

    private static class GetAllCoursesAsync extends AsyncTask<Void, Void, List<Course>> {
        private CourseDao courseDao;
        private CoursesCallback callback;
        private Exception exception;

        GetAllCoursesAsync(CourseDao courseDao, CoursesCallback callback) {
            this.courseDao = courseDao;
            this.callback = callback;
        }

        @Override
        protected List<Course> doInBackground(Void... voids) {
            try {
                return courseDao.getAllCourses();
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Course> courses) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onCoursesLoaded(courses);
            }
        }
    }

    private static class RegisterForCourseAsync extends AsyncTask<String, Void, Boolean> {
        private CourseDao courseDao;
        private EnrollmentDao enrollmentDao;
        private RegistrationCallback callback;
        private String errorMessage;

        RegisterForCourseAsync(CourseDao courseDao, EnrollmentDao enrollmentDao, RegistrationCallback callback) {
            this.courseDao = courseDao;
            this.enrollmentDao = enrollmentDao;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String studentId = params[0];
            String courseCode = params[1];

            try {
                // Check if course is available
                int isAvailable = courseDao.isCourseAvailable(courseCode);
                if (isAvailable == 0) {
                    errorMessage = "Course is full or not available";
                    return false;
                }

                // Check if student is already enrolled
                int isEnrolled = enrollmentDao.isStudentAlreadyEnrolled(studentId, courseCode);
                if (isEnrolled > 0) {
                    errorMessage = "You are already enrolled in this course";
                    return false;
                }

                // Create enrollment
                Enrollment enrollment = new Enrollment(studentId, courseCode, "REGISTERED");
                long enrollmentId = enrollmentDao.insertEnrollment(enrollment);

                if (enrollmentId > 0) {
                    // Update course enrollment count
                    int updated = courseDao.enrollStudentInCourse(courseCode);
                    return updated > 0;
                }

                return false;

            } catch (Exception e) {
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onRegistrationSuccess();
            } else {
                callback.onRegistrationFailed(errorMessage != null ? errorMessage : "Registration failed");
            }
        }
    }

    private static class GetStudentEnrollmentsAsync extends AsyncTask<String, Void, List<Enrollment>> {
        private EnrollmentDao enrollmentDao;
        private EnrollmentsCallback callback;
        private Exception exception;

        GetStudentEnrollmentsAsync(EnrollmentDao enrollmentDao, EnrollmentsCallback callback) {
            this.enrollmentDao = enrollmentDao;
            this.callback = callback;
        }

        @Override
        protected List<Enrollment> doInBackground(String... studentIds) {
            try {
                return enrollmentDao.getEnrollmentsByStudent(studentIds[0]);
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Enrollment> enrollments) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onEnrollmentsLoaded(enrollments);
            }
        }
    }

    private static class DropCourseAsync extends AsyncTask<String, Void, Boolean> {
        private CourseDao courseDao;
        private EnrollmentDao enrollmentDao;
        private RegistrationCallback callback;
        private String errorMessage;

        DropCourseAsync(CourseDao courseDao, EnrollmentDao enrollmentDao, RegistrationCallback callback) {
            this.courseDao = courseDao;
            this.enrollmentDao = enrollmentDao;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            int enrollmentId = Integer.parseInt(params[0]); // Convert back to int
            String courseCode = params[1];

            try {
                // Update enrollment status to DROPPED
                int updated = enrollmentDao.updateStatus(enrollmentId, "DROPPED");
                if (updated > 0) {
                    // Decrement course enrollment count
                    int decremented = courseDao.decrementEnrolledStudents(courseCode);
                    return decremented > 0;
                }
                return false;

            } catch (Exception e) {
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onRegistrationSuccess();
            } else {
                callback.onRegistrationFailed(errorMessage != null ? errorMessage : "Failed to drop course");
            }
        }
    }

    // This method get course details by code
    public void getCourseByCode(String courseCode, CourseCallback callback) {
        new GetCourseByCodeAsync(courseDao, callback).execute(courseCode);
    }

    public interface CourseCallback {
        void onCourseLoaded(Course course);
        void onError(Exception e);
    }

    private static class GetCourseByCodeAsync extends AsyncTask<String, Void, Course> {
        private CourseDao courseDao;
        private CourseCallback callback;
        private Exception exception;

        GetCourseByCodeAsync(CourseDao courseDao, CourseCallback callback) {
            this.courseDao = courseDao;
            this.callback = callback;
        }

        @Override
        protected Course doInBackground(String... courseCodes) {
            try {
                return courseDao.getCourseByCode(courseCodes[0]);
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Course course) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onCourseLoaded(course);
            }
        }
    }

    // Add method to get course name by code
    public void getCourseNameByCode(String courseCode, CourseNameCallback callback) {
        new GetCourseNameAsync(courseDao, callback).execute(courseCode);
    }

    // Add method to get course credits by code
    public void getCourseCreditsByCode(String courseCode, CourseCreditsCallback callback) {
        new GetCourseCreditsAsync(courseDao, callback).execute(courseCode);
    }

    // Add callback interfaces
    public interface CourseNameCallback {
        void onCourseNameLoaded(String courseName);
        void onError(Exception e);
    }

    public interface CourseCreditsCallback {
        void onCourseCreditsLoaded(int credits);
        void onError(Exception e);
    }

    // Add AsyncTasks for course name and credits
    private static class GetCourseNameAsync extends AsyncTask<String, Void, String> {
        private CourseDao courseDao;
        private CourseNameCallback callback;
        private Exception exception;

        GetCourseNameAsync(CourseDao courseDao, CourseNameCallback callback) {
            this.courseDao = courseDao;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... courseCodes) {
            try {
                Course course = courseDao.getCourseByCode(courseCodes[0]);
                return course != null ? course.getCourseName() : "Course Not Found";
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String courseName) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onCourseNameLoaded(courseName);
            }
        }
    }

    private static class GetCourseCreditsAsync extends AsyncTask<String, Void, Integer> {
        private CourseDao courseDao;
        private CourseCreditsCallback callback;
        private Exception exception;

        GetCourseCreditsAsync(CourseDao courseDao, CourseCreditsCallback callback) {
            this.courseDao = courseDao;
            this.callback = callback;
        }

        @Override
        protected Integer doInBackground(String... courseCodes) {
            try {
                Course course = courseDao.getCourseByCode(courseCodes[0]);
                return course != null ? course.getCredits() : 0;
            } catch (Exception e) {
                exception = e;
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer credits) {
            if (exception != null) {
                callback.onError(exception);
            } else {
                callback.onCourseCreditsLoaded(credits);
            }
        }
    }
}