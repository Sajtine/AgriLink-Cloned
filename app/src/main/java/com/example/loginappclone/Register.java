package com.example.loginappclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends AppCompatActivity {

    private EditText reg_username, reg_phoneNumber;
    private Spinner userType;
    MyDatabaseHelper databaseHelper;
    private EditText reg_password, reg_confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        databaseHelper = new MyDatabaseHelper(this);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Initialize fields
        reg_username = findViewById(R.id.username);
        reg_phoneNumber = findViewById(R.id.phone);
        userType = findViewById(R.id.userTypeSpinner);
        reg_password = findViewById(R.id.password);
        reg_confirmPassword = findViewById(R.id.confirmPassword);


        Button register = findViewById(R.id.submit);
        TextView userLogin = findViewById(R.id.user_login);

        register.setOnClickListener(v -> getUserInfo());

        userLogin.setOnClickListener(v ->
                startActivity(new Intent(Register.this, MainActivity.class)));

        setupPasswordToggle(reg_password);
        setupPasswordToggle(reg_confirmPassword);
    }

    // Register User
    public void getUserInfo() {
        String username = reg_username.getText().toString().trim();
        String phoneNumber = reg_phoneNumber.getText().toString().trim();
        String selectedRole = userType.getSelectedItem().toString();
        String password = reg_password.getText().toString().trim();
        String confirmPassword = reg_confirmPassword.getText().toString().trim();

        if (username.isEmpty() || phoneNumber.isEmpty() || selectedRole.equals("Select user type") || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill up all fields including user type!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)){
            Toast.makeText(this, "Password do not match!", Toast.LENGTH_SHORT).show();
        }else{
            // Save user in Firebase
            databaseHelper.registerUser(username, phoneNumber, selectedRole, password, this);
        }

    }


    private void setupPasswordToggle(EditText editText) {
        final boolean[] isPasswordVisible = {false};

        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight()
                        - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()
                        - editText.getPaddingRight())) {

                    v.performClick(); // accessibility support
                    togglePasswordVisibility(editText, isPasswordVisible);
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText editText, boolean[] isPasswordVisible) {
        Drawable[] drawables = editText.getCompoundDrawables();
        Drawable currentLeft = drawables[0]; // keep existing left drawable (if any)

        if (!isPasswordVisible[0]) {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(
                    currentLeft, // do not add or change
                    null,
                    ContextCompat.getDrawable(this, R.drawable.eye),
                    null);
        } else {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(
                    currentLeft, // do not add or change
                    null,
                    ContextCompat.getDrawable(this, R.drawable.closed_eye),
                    null);
        }

        isPasswordVisible[0] = !isPasswordVisible[0];
        editText.setSelection(editText.getText().length());
    }
}
