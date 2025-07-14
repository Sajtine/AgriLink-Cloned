package com.example.loginapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText userEmail, userPassword;
    private Button login;
    private TextView next;
    private SharedPreferences sharedPreferences;

    MyDatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Transparent Status Bar
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String role = sharedPreferences.getString("role", "");

        if (isLoggedIn) {
            if (role.equalsIgnoreCase("vendor")) {
                startActivity(new Intent(MainActivity.this, Vendor.class));
                finish();
            } else if (role.equalsIgnoreCase("farmer")) {
                startActivity(new Intent(MainActivity.this, Home.class));
                finish();
            }
        }

        userEmail = findViewById(R.id.email);
        userPassword = findViewById(R.id.password);
        login = findViewById(R.id.submit);
        next = findViewById(R.id.register);

        // Login Button Listener
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Register.class));
            }
        });
    }

    // Validation
    public void validation() {

        databaseHelper = new MyDatabaseHelper(this);

        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = databaseHelper.checkUserRole(email, password);

        if (role != null) {
            String[] userInfo = role.split(",");
            String userId = userInfo[0];
            String username = userInfo[1];
            String userRole = userInfo[2];

            sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

            // Save to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("email", email);
            editor.putString("role", userRole);
            editor.putString("userId", userId); // optional: save userId
            editor.putString("username", username);
            editor.apply();

            Toast.makeText(MainActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Welcome " + username + "!", Toast.LENGTH_SHORT).show();

            int vendorId = Integer.parseInt(userId);

            // If vendor, save vendor_id and name to markets table
            if (userRole.equalsIgnoreCase("vendor")) {

                // Check first if already exists to avoid duplicate
                if (!databaseHelper.isVendorInMarkets(userId)) {
                    databaseHelper.addVendorToMarkets(Integer.parseInt(userId), username);
                }

                // Check if the vendor completes the info
                if(!databaseHelper.isVendorInfoComplete(vendorId)){

                    Intent intent = new Intent(MainActivity.this, Vendor_Info.class);
                    intent.putExtra("info_complete", false);
                    startActivity(intent);
                    finish();

                }else{
                    startActivity(new Intent(MainActivity.this, Vendor.class));
                    finish();
                }

            } else if (userRole.equalsIgnoreCase("farmer")) {
                startActivity(new Intent(MainActivity.this, Home.class));
            } else {
                startActivity(new Intent(MainActivity.this, Home.class));
            }

            finish();
        }else{
            Toast.makeText(this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
        }


    }


}