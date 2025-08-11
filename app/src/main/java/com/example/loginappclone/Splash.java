package com.example.loginappclone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    private ImageView logo;
    private static final int ANIMATION_DURATION = 1200;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        logo = findViewById(R.id.logo);

        // Start the animation when the activity is created
        startAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimation(); // Restart the animation when the app is reopened
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove any pending callbacks to avoid memory leaks
        handler.removeCallbacksAndMessages(null);
    }

    private void startAnimation() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        logo.startAnimation(fadeIn);

        handler.postDelayed(() -> {
            logo.startAnimation(fadeOut);
            logo.setVisibility(ImageView.INVISIBLE);
        }, 2000);

        handler.postDelayed(() -> {
            Intent intent = new Intent(Splash.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, ANIMATION_DURATION);
    }
}