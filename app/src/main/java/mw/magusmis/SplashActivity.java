package mw.magusmis;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import mw.magusmis.activities.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No setContentView - we use the theme's windowBackground

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DURATION);
    }

}