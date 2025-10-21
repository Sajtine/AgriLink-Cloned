package com.example.loginappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText phone_number, user_password;
    private Button login_button;
    private TextView register, forgotPassword;
    String formattedNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        phone_number = findViewById(R.id.phone_number);
        user_password = findViewById(R.id.password);
        login_button = findViewById(R.id.submit);
        register = findViewById(R.id.register);
        forgotPassword = findViewById(R.id.forgotPassword);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Check if user already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", null);

        if (role != null) {
            // User already logged in, redirect based on role
            if (role.equals("farmers")) {
                startActivity(new Intent(MainActivity.this, Home.class));
            } else {
                startActivity(new Intent(MainActivity.this, Vendor.class)); // your vendor activity
            }
            finish();
        }

        // Forgot Password
        forgotPassword.setOnClickListener(v -> startActivity(
                new Intent(MainActivity.this, ForgotPasswordActivity.class)
        ));

        login_button.setOnClickListener(v -> login());

        register.setOnClickListener(v -> startActivity(
                new Intent(MainActivity.this, Register.class)
        ));

        setupPasswordToggle();
    }


    private void login(){
        String phoneNumber = phone_number.getText().toString().trim();
        String password = user_password.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill up all the fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        dbHelper.loginUser(phoneNumber, password);

    }

    private void setupPasswordToggle() {
        final boolean[] isPasswordVisible = {false};
        Drawable leftDrawable = getResources().getDrawable(R.drawable.password);

        user_password.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (user_password.getRight() -
                        user_password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() -
                        user_password.getPaddingRight())) {

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
            user_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            user_password.setCompoundDrawablesWithIntrinsicBounds(
                    leftDrawable,
                    null,
                    getResources().getDrawable(R.drawable.eye),
                    null);
        } else {
            // Hide password
            user_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            user_password.setCompoundDrawablesWithIntrinsicBounds(
                    leftDrawable,
                    null,
                    getResources().getDrawable(R.drawable.closed_eye),
                    null);
        }
        isPasswordVisible[0] = !isPasswordVisible[0];
        user_password.setSelection(user_password.getText().length());
    }


}
