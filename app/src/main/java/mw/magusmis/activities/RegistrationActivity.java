package mw.magusmis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import mw.magusmis.R;
import mw.magusmis.models.Student;
import mw.magusmis.repository.StudentRepository;
import mw.magusmis.utils.SecurityUtils;
import mw.magusmis.utils.SessionManager;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etEmail, etPhone, etDepartment, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private StudentRepository studentRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initializeViews();
        setClickListeners();
    }

    private void initializeViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDepartment = findViewById(R.id.etDepartment);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        studentRepository = new StudentRepository(this);
        sessionManager = new SessionManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void attemptRegistration() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (validateInputs(firstName, lastName, email, phone, department, password, confirmPassword)) {
            showLoading(true);

            studentRepository.checkEmailExists(email, new StudentRepository.CheckExistsCallback() {
                @Override
                public void onExists(boolean exists) {
                    if (exists) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            etEmail.setError("Email already registered");
                        });
                    } else {
                        registerNewStudent(firstName, lastName, email, phone, department, password);
                    }
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(RegistrationActivity.this,
                                "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void registerNewStudent(String firstName, String lastName, String email,
                                    String phone, String department, String password) {
        String studentId = "S" + System.currentTimeMillis();
        String passwordHash = SecurityUtils.hashPassword(password);

        Student student = new Student(studentId, firstName, lastName, email, passwordHash, department);
        student.setPhoneNumber(phone);

        studentRepository.insertStudent(student, new StudentRepository.DatabaseOperationCallback() {
            @Override
            public void onOperationComplete(long result) {
                runOnUiThread(() -> {
                    showLoading(false);

                    // Verify the student was actually saved by querying it back
                    studentRepository.authenticateStudent(email, passwordHash,
                            new StudentRepository.AuthenticationCallback() {
                                @Override
                                public void onAuthenticationSuccess(Student savedStudent) {
                                    sessionManager.createLoginSession(savedStudent);
                                    Toast.makeText(RegistrationActivity.this,
                                            "Registration successful! Student ID: " + studentId,
                                            Toast.LENGTH_LONG).show();
                                    navigateToMainActivity();
                                }

                                @Override
                                public void onAuthenticationFailed() {
                                    // This should not happen after successful registration
                                    Toast.makeText(RegistrationActivity.this,
                                            "Registration completed but login verification failed. Please try logging in.",
                                            Toast.LENGTH_LONG).show();
                                    navigateToLogin();
                                }
                            });
                });
            }

            @Override
            public void onOperationFailed(Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    String errorMessage = "Registration failed: " + e.getMessage();
                    if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                        errorMessage = "Email already registered. Please use a different email.";
                    }
                    Toast.makeText(RegistrationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean validateInputs(String firstName, String lastName, String email,
                                   String phone, String department, String password, String confirmPassword) {
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            return false;
        }

        if (department.isEmpty()) {
            etDepartment.setError("Department is required");
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnRegister.setText("Registering...");
            btnRegister.setEnabled(false);
        } else {
            btnRegister.setText("Register");
            btnRegister.setEnabled(true);
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}