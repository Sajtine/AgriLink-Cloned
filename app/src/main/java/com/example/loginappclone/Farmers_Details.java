package com.example.loginappclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Farmers_Details extends AppCompatActivity {
    private EditText fullNameInput, addressInput, phoneInput, emailInput;
    private Button saveButton, mapSelection;
    private String latitude, longitude;

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
        mapSelection = findViewById(R.id.mapSelection);

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

                // Redirect only after a successful update
                boolean shouldDirectToMarket = getIntent().getBooleanExtra("redirectToMarket", false);

                if (shouldDirectToMarket) {
                    Intent intent = new Intent(Farmers_Details.this, Market_Location.class);
                    startActivity(intent);
                }

                finish(); // Close this activity
            } else {
                Toast.makeText(this, "Failed to update details", Toast.LENGTH_SHORT).show();
            }
        });


        mapSelection.setOnClickListener(v -> {
            Intent intent = new Intent(this, FarmerMapPicker.class);
            startActivityForResult(intent, 1001);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("latitude", 0.0);
            double lng = data.getDoubleExtra("longitude", 0.0);

            latitude = String.valueOf(lat);
            longitude = String.valueOf(lng);

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> adddressList = geocoder.getFromLocation(lat, lng, 1);
                Address address = adddressList.get(0);

                String barangay = address.getSubLocality();
                String streetName = address.getThoroughfare();
                String city = address.getLocality();

                String fullAddress = streetName + ", " + barangay + ", " + city;

                addressInput.setText(fullAddress);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
            }
        }
    }
}