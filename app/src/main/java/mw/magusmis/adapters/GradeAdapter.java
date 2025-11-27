package mw.magusmis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import mw.magusmis.R;
import mw.magusmis.models.Enrollment;

import java.util.List;
import java.util.Map;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<Enrollment> enrollmentList;
    private Map<String, String> courseNameCache;

    public GradeAdapter(List<Enrollment> enrollmentList, Map<String, String> courseNameCache) {
        this.enrollmentList = enrollmentList;
        this.courseNameCache = courseNameCache;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        Enrollment enrollment = enrollmentList.get(position);
        holder.bind(enrollment, courseNameCache);
    }

    @Override
    public int getItemCount() {
        return enrollmentList.size();
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCourseCode, tvCourseName, tvGrade, tvGradeLetter, tvStatus;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvGrade = itemView.findViewById(R.id.tvGrade);
            tvGradeLetter = itemView.findViewById(R.id.tvGradeLetter);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Enrollment enrollment, Map<String, String> courseNameCache) {
            tvCourseCode.setText(enrollment.getCourseCode());

            // Get course name from cache - if not found, use course code as fallback
            String courseName = courseNameCache.get(enrollment.getCourseCode());
            if (courseName != null) {
                tvCourseName.setText(courseName);
            } else {
                tvCourseName.setText("Loading...");
            }

            if (enrollment.getGrade() != null) {
                tvGrade.setText(String.format("%.1f", enrollment.getGrade()));
                tvGradeLetter.setText(enrollment.getGradeLetter());
                tvStatus.setText("Completed");
                tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
            } else {
                tvGrade.setText("--");
                tvGradeLetter.setText("--");
                tvStatus.setText("In Progress");
                tvStatus.setBackgroundResource(R.drawable.bg_status_in_progress);
            }
        }
    }
}