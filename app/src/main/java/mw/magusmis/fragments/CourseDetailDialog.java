package mw.magusmis.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import mw.magusmis.R;
import mw.magusmis.models.Course;
import mw.magusmis.repository.CourseRepository;
import mw.magusmis.utils.SessionManager;

public class CourseDetailDialog extends DialogFragment {

    private static final String ARG_COURSE = "course";
    private Course course;
    private CourseRepository courseRepository;
    private SessionManager sessionManager;
    private CourseRegistrationListener registrationListener;

    public interface CourseRegistrationListener {
        void onCourseRegistered(Course course);
    }

    public static CourseDetailDialog newInstance(Course course) {
        CourseDetailDialog fragment = new CourseDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COURSE, course);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCourseRegistrationListener(CourseRegistrationListener listener) {
        this.registrationListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            course = (Course) getArguments().getSerializable(ARG_COURSE);
        }

        courseRepository = new CourseRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_course_detail, null);

        initializeViews(view);

        builder.setView(view)
                .setTitle("Course Details")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    private void initializeViews(View view) {
        if (course == null) return;

        TextView tvCourseCode = view.findViewById(R.id.tvCourseCode);
        TextView tvCourseName = view.findViewById(R.id.tvCourseName);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvInstructor = view.findViewById(R.id.tvInstructor);
        TextView tvSchedule = view.findViewById(R.id.tvSchedule);
        TextView tvCredits = view.findViewById(R.id.tvCredits);
        TextView tvDepartment = view.findViewById(R.id.tvDepartment);
        TextView tvSemester = view.findViewById(R.id.tvSemester);
        TextView tvSeats = view.findViewById(R.id.tvSeats);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        tvCourseCode.setText(course.getCourseCode());
        tvCourseName.setText(course.getCourseName());
        tvDescription.setText(course.getDescription() != null ? course.getDescription() : "No description available");
        tvInstructor.setText(course.getInstructor() != null ? course.getInstructor() : "TBA");
        tvSchedule.setText(course.getSchedule() != null ? course.getSchedule() : "Schedule TBA");
        tvCredits.setText(String.valueOf(course.getCredits()));
        tvDepartment.setText(course.getDepartment() != null ? course.getDepartment() : "General");
        tvSemester.setText(course.getSemester() != null ? course.getSemester() : "Current Semester");

        String seatsInfo = course.getEnrolledStudents() + "/" + course.getCapacity() +
                " (" + course.getAvailableSeats() + " available)";
        tvSeats.setText(seatsInfo);

        // Update button based on course state
        if (course.isRegistered()) {
            btnRegister.setText("Already Registered");
            btnRegister.setEnabled(false);
        } else if (course.isLoading()) {
            btnRegister.setText("Registering...");
            btnRegister.setEnabled(false);
        } else if (course.hasAvailableSeats()) {
            btnRegister.setText("Register for Course");
            btnRegister.setEnabled(true);
            btnRegister.setOnClickListener(v -> registerForCourse(btnRegister));
        } else {
            btnRegister.setText("Course Full");
            btnRegister.setEnabled(false);
        }
    }

    private void registerForCourse(Button btnRegister) {
        String studentId = sessionManager.getStudentId();
        if (studentId.isEmpty()) {
            Toast.makeText(getContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setText("Registering...");
        btnRegister.setEnabled(false);

        courseRepository.registerForCourse(studentId, course.getCourseCode(), new CourseRepository.RegistrationCallback() {
            @Override
            public void onRegistrationSuccess() {
                if (getContext() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Successfully registered for " + course.getCourseCode(), Toast.LENGTH_SHORT).show();
                        btnRegister.setText("Registered");
                        btnRegister.setEnabled(false);

                        if (registrationListener != null) {
                            registrationListener.onCourseRegistered(course);
                        }

                        dismiss();
                    });
                }
            }

            @Override
            public void onRegistrationFailed(String error) {
                if (getContext() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        btnRegister.setText("Register for Course");
                        btnRegister.setEnabled(true);
                    });
                }
            }
        });
    }
}