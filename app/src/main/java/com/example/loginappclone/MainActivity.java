package com.example.loginappclone;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText userEmail, userPassword;
    private Button loginButton;
    private TextView registerTextView, forgotPassword;
    private SharedPreferences sharedPreferences;
    private MyDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Initialize UI components
        initViews();

        // Setup transparent status bar
        setupStatusBar();

        // Check if user is already logged in
        checkExistingSession();

        // Setup password visibility toggle
        setupPasswordToggle();

        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
//        userEmail = findViewById(R.id.email);
//        userPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.submit);
        registerTextView = findViewById(R.id.register);
        databaseHelper = new MyDatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
//        forgotPassword = findViewById(R.id.forgotPassword);
    }

    private void setupStatusBar() {
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void checkExistingSession() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String role = sharedPreferences.getString("role", "");

        if (isLoggedIn) {
            redirectBasedOnRole(role);
        }
    }

    private void redirectBasedOnRole(String role) {
        Class<?> destination = role.equalsIgnoreCase("vendor") ? Vendor.class : Home.class;
        startActivity(new Intent(MainActivity.this, destination));
        finish();
    }

    private void setupPasswordToggle() {
        final boolean[] isPasswordVisible = {false};
        Drawable leftDrawable = getResources().getDrawable(R.drawable.password);

        userPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (userPassword.getRight() -
                        userPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() -
                        userPassword.getPaddingRight())) {

                    v.performClick(); // Accessibility support
                    togglePasswordVisibility(isPasswordVisible, leftDrawable);
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(boolean[] isPasswordVisible, Drawable leftDrawable) {
        if (!isPasswordVisible[0]) {
            // Show password
            userPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            userPassword.setCompoundDrawablesWithIntrinsicBounds(
                    leftDrawable,
                    null,
                    getResources().getDrawable(R.drawable.eye),
                    null);
        } else {
            // Hide password
            userPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            userPassword.setCompoundDrawablesWithIntrinsicBounds(
                    leftDrawable,
                    null,
                    getResources().getDrawable(R.drawable.closed_eye),
                    null);
        }
        isPasswordVisible[0] = !isPasswordVisible[0];
        userPassword.setSelection(userPassword.getText().length());
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> validateAndLogin());
        registerTextView.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Register.class)));

//        forgotPassword.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class)));
    }

    private void validateAndLogin() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Please enter all fields!");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address");
            return;
        }

        databaseHelper.loginUser(email, password, this);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}