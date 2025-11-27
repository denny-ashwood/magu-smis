package mw.magusmis.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import androidx.sqlite.db.SupportSQLiteDatabase;

import mw.magusmis.database.dao.CourseDao;
import mw.magusmis.database.dao.EnrollmentDao;
import mw.magusmis.database.dao.StudentDao;
import mw.magusmis.models.Student;
import mw.magusmis.models.Course;
import mw.magusmis.models.Enrollment;

@Database(
        entities = {Student.class, Course.class, Enrollment.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract StudentDao studentDao();
    public abstract CourseDao courseDao();
    public abstract EnrollmentDao enrollmentDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "magu_smis.db"
                            ).fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    new PopulateDbAsync(INSTANCE).execute();
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final StudentDao studentDao;
        private final CourseDao courseDao;
        private final EnrollmentDao enrollmentDao;

        PopulateDbAsync(AppDatabase db) {
            studentDao = db.studentDao();
            courseDao = db.courseDao();
            enrollmentDao = db.enrollmentDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // Clear existing data
            studentDao.deleteAllStudents();
            courseDao.deleteAllCourses();
            enrollmentDao.deleteAllEnrollments();

            // Add demo student
            Student demoStudent = new Student("S001", "John", "Doe",
                    "demo@magu.edu", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
                    "Computer Science");
            demoStudent.setPhoneNumber("+1 234 567 8900");
            studentDao.insert(demoStudent);

            // Add demo courses
            Course course1 = new Course("BIS1011", "Programming Fundamentals",
                    "Basic programming concepts and problem-solving techniques", 3,
                    "Computer Science", "Prof. Andrew Kasulo", "Mon/Wed 10:00-11:30", 30, 15, "1/2");
            courseDao.insert(course1);

            Course course2 = new Course("CCC1011", "Business Mathematics",
                    "Differential and integral calculus", 4, "Commerce",
                    "Prof. Stephen Kalisha", "Tue/Thu 13:00-14:30", 40, 25, "1/2");
            courseDao.insert(course2);

            Course course3 = new Course("BIS3011", "Data Structures & Algorithms",
                    "Software Engineering principles and practices", 4, "Computer Science",
                    "Prof. Stephen Kalisha", "Mon/Wed/Fri 09:00-10:00", 35, 10, "4/4");
            courseDao.insert(course3);

            Course course4 = new Course("BIS4012", "Artificial Intelligence",
                    "Artificial Intelligence, Machine Learning", 3, "Computer Science",
                    "Prof. Andrew Kasulo", "Tue/Thu 11:00-12:30", 25, 20, "4/4");
            courseDao.insert(course4);

            Course course5 = new Course("BIS4011", "Business Intelligence",
                    "Business Intelligence and Analytics", 4, "Computer Science",
                    "Prof. Andrew Kasulo", "Tue/Thu 09:00-10:30", 35, 12, "4/4");
            courseDao.insert(course5);

            Course course6 = new Course("BIS2012", "Java",
                    "Java Programming language and Object-Oriented Programming", 3, "Computer Science",
                    "Prof. Stephen Kalisha", "Mon/Wed/Fri 14:00-15:00", 28, 18, "2/2");
            courseDao.insert(course6);

            Course course7 = new Course("BIS3023", "Web Programming",
                    "Web programming and development", 3, "Computer Science",
                    "Prof. Stephen Kalisha", "Tue/Thu 16:00-17:30", 20, 8, "3/2");
            courseDao.insert(course7);

            // Add demo enrollments
            Enrollment enrollment1 = new Enrollment("S001", "BIS3023", "COMPLETED");
            enrollment1.setGrade(85.5);
            enrollmentDao.insert(enrollment1);

            Enrollment enrollment2 = new Enrollment("S001", "BIS2012", "COMPLETED");
            enrollment2.setGrade(92.0);
            enrollmentDao.insert(enrollment2);

            Enrollment enrollment3 = new Enrollment("S001", "BIS4012", "REGISTERED");
            enrollmentDao.insert(enrollment3);

            return null;
        }
    }
}