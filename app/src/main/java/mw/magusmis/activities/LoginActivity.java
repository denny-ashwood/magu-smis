package mw.magusmis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import mw.magusmis.R;
import mw.magusmis.models.Student;
import mw.magusmis.repository.StudentRepository;
import mw.magusmis.utils.SecurityUtils;
import mw.magusmis.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private StudentRepository studentRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        checkExistingSession();
        setClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        studentRepository = new StudentRepository(this);
        sessionManager = new SessionManager(this);

        // Remove demo credential pre-fill for real scenarios
        // etEmail.setText("demo@magu.edu");
        // etPassword.setText("password");
    }

    private void checkExistingSession() {
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity();
        }
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> navigateToRegistration());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            showLoading(true);

            String passwordHash = SecurityUtils.hashPassword(password);

            studentRepository.authenticateStudent(email, passwordHash,
                    new StudentRepository.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSuccess(Student student) {
                            runOnUiThread(() -> {
                                showLoading(false);
                                sessionManager.createLoginSession(student);
                                loginSuccess(student);
                            });
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            runOnUiThread(() -> {
                                showLoading(false);
                                // Provide helpful error message
                                String message = "Invalid email or password.\n" +
                                        "Please check your credentials or register if you're a new user.";
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
        }
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return false;
        }

        return true;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setText("Logging in...");
            btnLogin.setEnabled(false);
        } else {
            btnLogin.setText("Login");
            btnLogin.setEnabled(true);
        }
    }

    private void loginSuccess(Student student) {
        Toast.makeText(this, "Welcome, " + student.getFirstName() + "!", Toast.LENGTH_SHORT).show();
        navigateToMainActivity();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToRegistration() {
        startActivity(new Intent(this, RegistrationActivity.class));
    }
}