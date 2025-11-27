package mw.magusmis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import mw.magusmis.R;
import mw.magusmis.models.Course;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private OnCourseClickListener onCourseClickListener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
        void onRegisterClick(Course course, int position);
    }

    public CourseAdapter(List<Course> courseList, OnCourseClickListener listener) {
        this.courseList = courseList;
        this.onCourseClickListener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.bind(course, position);

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (onCourseClickListener != null) {
                onCourseClickListener.onCourseClick(course);
            }
        });

        // Set click listener specifically for the register button
        holder.btnRegister.setOnClickListener(v -> {
            if (onCourseClickListener != null) {
                onCourseClickListener.onRegisterClick(course, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCourseCode, tvCourseName, tvInstructor, tvSchedule, tvSeats;
        private Button btnRegister;
        private int currentPosition;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            btnRegister = itemView.findViewById(R.id.btnRegister);
        }

        public void bind(Course course, int position) {
            this.currentPosition = position;

            tvCourseCode.setText(course.getCourseCode());
            tvCourseName.setText(course.getCourseName());
            tvInstructor.setText(course.getInstructor());
            tvSchedule.setText(course.getSchedule());

            String seatsInfo = course.getEnrolledStudents() + "/" + course.getCapacity() + " enrolled";
            tvSeats.setText(seatsInfo);

            if (course.isRegistered()) {
                btnRegister.setText("Registered");
                btnRegister.setEnabled(false);
            } else if (course.isLoading()) {
                btnRegister.setText("Registering...");
                btnRegister.setEnabled(false);
            } else if (course.hasAvailableSeats()) {
                btnRegister.setText("Register");
                btnRegister.setEnabled(true);
            } else {
                btnRegister.setText("Full");
                btnRegister.setEnabled(false);
            }
        }
    }
}