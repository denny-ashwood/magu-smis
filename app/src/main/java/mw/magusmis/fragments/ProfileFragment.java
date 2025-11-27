package mw.magusmis.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import mw.magusmis.R;
import mw.magusmis.activities.LoginActivity;
import mw.magusmis.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private TextView tvStudentName, tvStudentId, tvDepartment, tvEmail, tvPhone, tvEnrollmentDate;
    private Button btnEditProfile, btnChangePassword, btnLogout;
    private SessionManager sessionManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(requireContext());
        initializeViews(view);
        setClickListeners();
        loadProfileData();

        return view;
    }

    private void initializeViews(View view) {
        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEnrollmentDate = view.findViewById(R.id.tvEnrollmentDate);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setClickListeners() {
        btnEditProfile.setOnClickListener(v -> onEditProfileClicked());
        btnChangePassword.setOnClickListener(v -> onChangePasswordClicked());
        btnLogout.setOnClickListener(v -> onLogoutClicked());
    }

    private void loadProfileData() {
        // Get real student data from session
        mw.magusmis.models.Student student = sessionManager.getStudentDetails();

        if (student != null) {
            tvStudentName.setText(student.getFullName());
            tvStudentId.setText(student.getStudentId());
            tvDepartment.setText(student.getDepartment() != null && !student.getDepartment().isEmpty()
                    ? student.getDepartment() : "Not specified");
            tvEmail.setText(student.getEmail());
            tvPhone.setText(student.getPhoneNumber() != null && !student.getPhoneNumber().isEmpty()
                    ? student.getPhoneNumber() : "Not provided");
            tvEnrollmentDate.setText(student.getEnrollmentDate() != null && !student.getEnrollmentDate().isEmpty()
                    ? student.getEnrollmentDate() : "Not available");
        } else {
            // Fallback if no student data found
            tvStudentName.setText("User not found");
            tvStudentId.setText("N/A");
            tvDepartment.setText("N/A");
            tvEmail.setText("N/A");
            tvPhone.setText("N/A");
            tvEnrollmentDate.setText("N/A");
        }
    }

    private void onEditProfileClicked() {
        // Navigate to edit profile activity
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), "Edit Profile feature coming soon", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void onChangePasswordClicked() {
        // Navigate to change password activity
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), "Change Password feature coming soon", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void onLogoutClicked() {
        // Clear user session and navigate to login
        sessionManager.logoutUser();

        // Verify logout was successful
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        } else {
            // Logout failed, show error
            android.widget.Toast.makeText(getContext(), "Logout failed. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}