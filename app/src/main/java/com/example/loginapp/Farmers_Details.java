package com.example.loginapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Farmers_Details extends AppCompatActivity {
    private EditText fullNameInput, addressInput, phoneInput, emailInput;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmers_details);

        // Initialize views
        fullNameInput = findViewById(R.id.fullNameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> {
            // Get values from input
            String name = fullNameInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            // Simple validation
            if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            MyDatabaseHelper db = new MyDatabaseHelper(this);
            boolean isUpdated = db.updateFarmerDetails(email, name, address, phone);

            if (isUpdated) {
                Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update details", Toast.LENGTH_SHORT).show();
            }

        });

        getFarmersDetails();
    }


    public void getFarmersDetails(){

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(this);
        Cursor cursor = databaseHelper.getUserDetails(email);

        if(cursor.moveToFirst()){

            String name = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String user_email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone_number = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

            fullNameInput.setText(name);
            emailInput.setText(user_email);
            phoneInput.setText(phone_number);
            addressInput.setText(address);
        }

        cursor.close();
        databaseHelper.close();
    }
}