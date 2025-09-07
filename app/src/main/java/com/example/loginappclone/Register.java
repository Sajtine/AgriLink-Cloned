package com.example.loginappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

        Button register = findViewById(R.id.submit);
        TextView userLogin = findViewById(R.id.user_login);

        register.setOnClickListener(v -> getUserInfo());

        userLogin.setOnClickListener(v ->
                startActivity(new Intent(Register.this, MainActivity.class)));
    }

    // Register User
    public void getUserInfo() {
        String username = reg_username.getText().toString().trim();
        String phoneNumber = reg_phoneNumber.getText().toString().trim();
        String selectedRole = userType.getSelectedItem().toString();

        if (username.isEmpty() || phoneNumber.isEmpty() || selectedRole.equals("Select user type")) {
            Toast.makeText(this, "Please fill up all fields including user type!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save user in Firebase
        databaseHelper.registerUser(username, phoneNumber, selectedRole, this);
    }
}
