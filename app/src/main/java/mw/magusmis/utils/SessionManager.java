package mw.magusmis.utils;

import android.content.Context;
import android.content.SharedPreferences;
import mw.magusmis.models.Student;

public class SessionManager {
    private static final String PREF_NAME = "MaguSmisPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_STUDENT_ID = "studentId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_LAST_NAME = "lastName";
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ENROLLMENT_DATE = "enrollmentDate";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(Student student) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_STUDENT_ID, student.getStudentId());
        editor.putString(KEY_EMAIL, student.getEmail());
        editor.putString(KEY_FIRST_NAME, student.getFirstName());
        editor.putString(KEY_LAST_NAME, student.getLastName());
        editor.putString(KEY_DEPARTMENT, student.getDepartment());
        editor.putString(KEY_PHONE, student.getPhoneNumber() != null ? student.getPhoneNumber() : "");
        editor.putString(KEY_ENROLLMENT_DATE, student.getEnrollmentDate() != null ? student.getEnrollmentDate() : "");
        editor.apply(); // Use apply() instead of commit() for better performance
    }

    public Student getStudentDetails() {
        if (!isLoggedIn()) {
            return null;
        }

        Student student = new Student();
        student.setStudentId(pref.getString(KEY_STUDENT_ID, ""));
        student.setEmail(pref.getString(KEY_EMAIL, ""));
        student.setFirstName(pref.getString(KEY_FIRST_NAME, ""));
        student.setLastName(pref.getString(KEY_LAST_NAME, ""));
        student.setDepartment(pref.getString(KEY_DEPARTMENT, ""));
        student.setPhoneNumber(pref.getString(KEY_PHONE, ""));
        student.setEnrollmentDate(pref.getString(KEY_ENROLLMENT_DATE, ""));

        return student;
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        // Clear all preferences
        editor.clear();
        // Use commit() here to ensure immediate write since we're logging out
        boolean success = editor.commit();

        if (!success) {
            // Fallback: clear using apply()
            editor.clear().apply();
        }
    }

    public String getStudentId() {
        return pref.getString(KEY_STUDENT_ID, "");
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }
}