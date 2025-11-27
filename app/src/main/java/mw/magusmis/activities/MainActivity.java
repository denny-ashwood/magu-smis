package mw.magusmis.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import mw.magusmis.R;
import mw.magusmis.fragments.DashboardFragment;
import mw.magusmis.fragments.CoursesFragment;
import mw.magusmis.fragments.GradesFragment;
import mw.magusmis.fragments.ProfileFragment;
import mw.magusmis.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check if user is logged in - redirect to login if not
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initializeViews();
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    // Update navigateToLogin method
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_courses) {
                selectedFragment = new CoursesFragment();
            } else if (itemId == R.id.nav_grades) {
                selectedFragment = new GradesFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}