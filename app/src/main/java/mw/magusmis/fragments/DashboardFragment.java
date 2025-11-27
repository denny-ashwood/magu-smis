package mw.magusmis.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import mw.magusmis.R;
import mw.magusmis.models.Course;
import mw.magusmis.repository.CourseRepository;
import mw.magusmis.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvWelcome, tvStudentInfo, tvCoursesCount, tvGPA, tvCredits, tvRecentActivity;
    private SessionManager sessionManager;
    private CourseRepository courseRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sessionManager = new SessionManager(requireContext());
        courseRepository = new CourseRepository(requireContext());
        initializeViews(view);
        loadDashboardData();

        return view;
    }

    private void initializeViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvStudentInfo = view.findViewById(R.id.tvStudentInfo);
        tvCoursesCount = view.findViewById(R.id.tvCoursesCount);
        tvGPA = view.findViewById(R.id.tvGPA);
        tvCredits = view.findViewById(R.id.tvCredits);
        tvRecentActivity = view.findViewById(R.id.tvRecentActivity);
    }

    private void loadDashboardData() {
        // Get real student data from session
        mw.magusmis.models.Student student = sessionManager.getStudentDetails();

        if (student != null) {
            // Show real user data
            tvWelcome.setText("Welcome, " + student.getFirstName() + "!");
            tvStudentInfo.setText("Department: " + student.getDepartment() + " | Student ID: " + student.getStudentId());

            // Load real academic data
            loadRealAcademicData(student.getStudentId());
        } else {
            // Fallback if no student data
            showNoStudentData();
        }
    }

    private void loadRealAcademicData(String studentId) {
        courseRepository.getStudentEnrollments(studentId, new CourseRepository.EnrollmentsCallback() {
            @Override
            public void onEnrollmentsLoaded(List<mw.magusmis.models.Enrollment> enrollments) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (enrollments == null || enrollments.isEmpty()) {
                            showNoEnrollmentsData();
                        } else {
                            // Load course details for each enrollment to get real credits
                            loadCourseDetailsForEnrollments(enrollments);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showNoEnrollmentsData();
                    });
                }
            }
        });
    }

    private void loadCourseDetailsForEnrollments(List<mw.magusmis.models.Enrollment> enrollments) {
        List<Course> courses = new ArrayList<>();
        final int[] coursesLoaded = {0};
        final int totalCourses = enrollments.size();

        if (totalCourses == 0) {
            showNoEnrollmentsData();
            return;
        }

        for (mw.magusmis.models.Enrollment enrollment : enrollments) {
            courseRepository.getCourseByCode(enrollment.getCourseCode(), new CourseRepository.CourseCallback() {
                @Override
                public void onCourseLoaded(Course course) {
                    courses.add(course);
                    coursesLoaded[0]++;

                    // When all courses are loaded, calculate statistics
                    if (coursesLoaded[0] == totalCourses) {
                        updateDashboardWithRealData(enrollments, courses);
                    }
                }

                @Override
                public void onError(Exception e) {
                    coursesLoaded[0]++;
                    // If we can't load a course, use default credits and continue
                    if (coursesLoaded[0] == totalCourses) {
                        updateDashboardWithRealData(enrollments, courses);
                    }
                }
            });
        }
    }

    private void updateDashboardWithRealData(List<mw.magusmis.models.Enrollment> enrollments, List<Course> courses) {
        // Create a map for quick course lookups
        java.util.Map<String, Course> courseMap = new java.util.HashMap<>();
        for (Course course : courses) {
            courseMap.put(course.getCourseCode(), course);
        }

        // Calculate real statistics
        int courseCount = enrollments.size();
        int totalCredits = 0;
        double totalGradePoints = 0;
        int gradedCourses = 0;
        StringBuilder recentActivity = new StringBuilder();

        for (mw.magusmis.models.Enrollment enrollment : enrollments) {
            // Get real credits from course data
            Course course = courseMap.get(enrollment.getCourseCode());
            int credits = 0;
            if (course != null) {
                credits = course.getCredits();
            }
            totalCredits += credits;

            // Calculate GPA
            if (enrollment.getGrade() != null) {
                double gradePoints = convertGradeToPoints(enrollment.getGrade());
                totalGradePoints += gradePoints;
                gradedCourses++;

                // Add to recent activity
                if (recentActivity.length() > 0) recentActivity.append("\n");
                recentActivity.append("• Grade received for ")
                        .append(enrollment.getCourseCode())
                        .append(": ").append(enrollment.getGradeLetter());
            }

            // Add registration to recent activity
            if ("REGISTERED".equals(enrollment.getStatus())) {
                if (recentActivity.length() > 0) recentActivity.append("\n");
                recentActivity.append("• Registered for ")
                        .append(enrollment.getCourseCode());
            }
        }

        // Calculate GPA
        double gpa = gradedCourses > 0 ? totalGradePoints / gradedCourses : 0.0;

        // If no recent activity, show default message
        if (recentActivity.length() == 0) {
            recentActivity.append("• No recent activity. Register for courses to get started!");
        }

        // Update UI with real data
        tvCoursesCount.setText(String.valueOf(courseCount));
        tvGPA.setText(String.format("%.2f", gpa));
        tvCredits.setText(String.valueOf(totalCredits));
        tvRecentActivity.setText(recentActivity.toString());
    }

    private void showNoStudentData() {
        tvWelcome.setText("Welcome!");
        tvStudentInfo.setText("Please log in to view your dashboard");
        tvCoursesCount.setText("0");
        tvGPA.setText("0.00");
        tvCredits.setText("0");
        tvRecentActivity.setText("Please log in to view your academic information");
    }

    private void showNoEnrollmentsData() {
        tvCoursesCount.setText("0");
        tvGPA.setText("0.00");
        tvCredits.setText("0");
        tvRecentActivity.setText("You haven't registered for any courses yet.\nVisit the Courses tab to get started!");
    }

    private double convertGradeToPoints(double grade) {
        if (grade >= 90) return 4.0;
        if (grade >= 80) return 3.0;
        if (grade >= 70) return 2.0;
        if (grade >= 60) return 1.0;
        return 0.0;
    }
}