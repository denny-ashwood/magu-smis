package mw.magusmis.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import mw.magusmis.R;
import mw.magusmis.adapters.CourseAdapter;
import mw.magusmis.models.Course;
import mw.magusmis.repository.CourseRepository;
import mw.magusmis.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoursesFragment extends Fragment {

    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;
    private List<Course> filteredCourseList;
    private TextInputEditText etSearch;
    private CourseRepository courseRepository;
    private SessionManager sessionManager;
    private Set<String> registeredCourseCodes = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSearch();
        loadCourses();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.rvCourses);
        etSearch = view.findViewById(R.id.etSearch);

        courseRepository = new CourseRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        filteredCourseList = new ArrayList<>();

        courseAdapter = new CourseAdapter(filteredCourseList, new CourseAdapter.OnCourseClickListener() {
            @Override
            public void onCourseClick(Course course) {
                // This is called when clicking anywhere on the course item (except the register button)
                System.out.println("DEBUG: Course item clicked: " + course.getCourseCode());
                showCourseDetailsDialog(course);
            }

            @Override
            public void onRegisterClick(Course course, int position) {
                // This is called specifically when clicking the register button
                System.out.println("DEBUG: Register button clicked for: " + course.getCourseCode() + " at position " + position);
                showCourseDetailsDialog(course);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(courseAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadCourses() {
        String studentId = sessionManager.getStudentId();

        courseRepository.getAvailableCourses(new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courses) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // First, load current registrations to check status
                        courseRepository.getStudentEnrollments(studentId, new CourseRepository.EnrollmentsCallback() {
                            @Override
                            public void onEnrollmentsLoaded(List<mw.magusmis.models.Enrollment> enrollments) {
                                // Build set of registered course codes
                                registeredCourseCodes.clear();
                                if (enrollments != null) {
                                    for (mw.magusmis.models.Enrollment enrollment : enrollments) {
                                        if ("REGISTERED".equals(enrollment.getStatus()) || "COMPLETED".equals(enrollment.getStatus())) {
                                            registeredCourseCodes.add(enrollment.getCourseCode());
                                        }
                                    }
                                }

                                // Now update courses with registration status
                                updateCoursesWithRegistrationStatus(courses);
                            }

                            @Override
                            public void onError(Exception e) {
                                // If we can't load enrollments, still show courses without registration status
                                updateCoursesWithRegistrationStatus(courses);
                            }
                        });
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error loading courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateCoursesWithRegistrationStatus(List<Course> courses) {
        courseList.clear();

        for (Course course : courses) {
            // Set registration status based on our tracked registrations
            course.setRegistered(registeredCourseCodes.contains(course.getCourseCode()));
            course.setLoading(false); // Reset loading state
            courseList.add(course);
        }

        filteredCourseList.clear();
        filteredCourseList.addAll(courseList);
        courseAdapter.notifyDataSetChanged();

        if (courseList.isEmpty()) {
            Toast.makeText(getContext(), "No available courses found", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterCourses(String query) {
        filteredCourseList.clear();
        if (query.isEmpty()) {
            filteredCourseList.addAll(courseList);
        } else {
            for (Course course : courseList) {
                if (course.getCourseName().toLowerCase().contains(query.toLowerCase()) ||
                        course.getCourseCode().toLowerCase().contains(query.toLowerCase()) ||
                        course.getInstructor().toLowerCase().contains(query.toLowerCase()) ||
                        course.getDepartment().toLowerCase().contains(query.toLowerCase())) {
                    filteredCourseList.add(course);
                }
            }
        }
        courseAdapter.notifyDataSetChanged();
    }

    private void showCourseDetailsDialog(Course course) {
        System.out.println("DEBUG: Opening course details dialog for: " + course.getCourseCode());

        CourseDetailDialog dialog = CourseDetailDialog.newInstance(course);
        dialog.setCourseRegistrationListener(new CourseDetailDialog.CourseRegistrationListener() {
            @Override
            public void onCourseRegistered(Course course) {
                // Refresh the courses list after registration
                System.out.println("DEBUG: Course registered, refreshing list");
                loadCourses();
            }
        });

        try {
            dialog.show(getParentFragmentManager(), "course_detail");
            System.out.println("DEBUG: Dialog shown successfully");
        } catch (Exception e) {
            System.out.println("DEBUG: Error showing dialog: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "Error opening course details", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh courses when fragment becomes visible again
        loadCourses();
    }
}