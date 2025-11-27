package mw.magusmis.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import mw.magusmis.R;
import mw.magusmis.adapters.GradeAdapter;
import mw.magusmis.models.Enrollment;
import mw.magusmis.repository.CourseRepository;
import mw.magusmis.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradesFragment extends Fragment {

    private RecyclerView recyclerView;
    private GradeAdapter gradeAdapter;
    private List<Enrollment> enrollmentList;
    private TextView tvCurrentGPA, tvTotalCredits;
    private CourseRepository courseRepository;
    private SessionManager sessionManager;

    // Cache for course data
    private Map<String, String> courseNameCache = new HashMap<>();
    private Map<String, Integer> courseCreditsCache = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grades, container, false);

        sessionManager = new SessionManager(requireContext());
        courseRepository = new CourseRepository(requireContext());
        initializeViews(view);
        setupRecyclerView();
        loadGradesData();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.rvGrades);
        tvCurrentGPA = view.findViewById(R.id.tvCurrentGPA);
        tvTotalCredits = view.findViewById(R.id.tvTotalCredits);
    }

    private void setupRecyclerView() {
        enrollmentList = new ArrayList<>();
        gradeAdapter = new GradeAdapter(enrollmentList, courseNameCache);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(gradeAdapter);
    }

    private void loadGradesData() {
        String studentId = sessionManager.getStudentId();
        if (studentId.isEmpty()) {
            Toast.makeText(getContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear caches
        courseNameCache.clear();
        courseCreditsCache.clear();

        courseRepository.getStudentEnrollments(studentId, new CourseRepository.EnrollmentsCallback() {
            @Override
            public void onEnrollmentsLoaded(List<Enrollment> enrollments) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (enrollments == null || enrollments.isEmpty()) {
                            showNoEnrollmentsData();
                        } else {
                            // Show ALL enrollments, not just completed ones
                            loadCourseDetailsForEnrollments(enrollments);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error loading academic records: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showNoEnrollmentsData();
                    });
                }
            }
        });
    }

    private void loadCourseDetailsForEnrollments(List<Enrollment> enrollments) {
        enrollmentList.clear();
        enrollmentList.addAll(enrollments);

        final int[] coursesLoaded = {0};
        final int totalCourses = enrollments.size();

        for (Enrollment enrollment : enrollments) {
            String courseCode = enrollment.getCourseCode();

            // Load course name for display
            courseRepository.getCourseNameByCode(courseCode, new CourseRepository.CourseNameCallback() {
                @Override
                public void onCourseNameLoaded(String courseName) {
                    courseNameCache.put(courseCode, courseName);
                    checkAllCourseDataLoaded(++coursesLoaded[0], totalCourses);
                }

                @Override
                public void onError(Exception e) {
                    courseNameCache.put(courseCode, "Course: " + courseCode);
                    checkAllCourseDataLoaded(++coursesLoaded[0], totalCourses);
                }
            });

            // Load course credits for GPA calculation
            courseRepository.getCourseCreditsByCode(courseCode, new CourseRepository.CourseCreditsCallback() {
                @Override
                public void onCourseCreditsLoaded(int credits) {
                    courseCreditsCache.put(courseCode, credits);
                }

                @Override
                public void onError(Exception e) {
                    courseCreditsCache.put(courseCode, 3); // Default credits
                }
            });
        }

        // Update UI immediately with basic data
        gradeAdapter.notifyDataSetChanged();
        calculateAndDisplayAcademicSummary(enrollments);
    }

    private void checkAllCourseDataLoaded(int coursesLoaded, int totalCourses) {
        if (coursesLoaded == totalCourses) {
            // All course names loaded, refresh the adapter
            gradeAdapter.notifyDataSetChanged();
        }
    }

    private void calculateAndDisplayAcademicSummary(List<Enrollment> enrollments) {
        double totalGradePoints = 0;
        int totalCredits = 0;
        int gradedCourses = 0;
        int totalEnrolledCourses = enrollments.size();

        for (Enrollment enrollment : enrollments) {
            // Only calculate GPA for courses with grades
            if (enrollment.getGrade() != null) {
                double gradePoints = convertGradeToPoints(enrollment.getGrade());
                totalGradePoints += gradePoints;
                gradedCourses++;

                // Get credits from cache
                Integer credits = courseCreditsCache.get(enrollment.getCourseCode());
                if (credits != null) {
                    totalCredits += credits;
                }
            }
        }

        // Calculate GPA (only for graded courses)
        double gpa = gradedCourses > 0 ? totalGradePoints / gradedCourses : 0.0;

        // Update UI
        tvCurrentGPA.setText(String.format("%.2f", gpa));
        tvTotalCredits.setText(String.valueOf(totalCredits));

        // Show helpful message if no grades yet
        if (gradedCourses == 0 && totalEnrolledCourses > 0) {
            Toast.makeText(getContext(),
                    "You have " + totalEnrolledCourses + " enrolled courses. Grades will appear here when available.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showNoEnrollmentsData() {
        enrollmentList.clear();
        gradeAdapter.notifyDataSetChanged();

        tvCurrentGPA.setText("0.00");
        tvTotalCredits.setText("0");

        Toast.makeText(getContext(),
                "You haven't enrolled in any courses yet. Visit the Courses tab to get started!",
                Toast.LENGTH_LONG).show();
    }

    private double convertGradeToPoints(double grade) {
        if (grade >= 90) return 4.0;
        if (grade >= 80) return 3.0;
        if (grade >= 70) return 2.0;
        if (grade >= 60) return 1.0;
        return 0.0;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGradesData();
    }
}