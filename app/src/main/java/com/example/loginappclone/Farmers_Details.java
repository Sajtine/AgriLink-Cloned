package com.example.loginappclone;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Farmers_Details extends AppCompatActivity {
    private EditText fullNameInput, addressInput, phoneInput, emailInput;
    private Button saveButton, mapSelection;
    private String latitude, longitude;
    private String currentUID;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmers_details);

        // SharedPreferences: get current user phone number
        currentUID = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("uid", null);

        if (currentUID == null) {
            // No session found, redirect to login
            startActivity(new Intent(Farmers_Details.this, MainActivity.class));
            finish();
            return;
        }

        // Firebase reference for farmers
        userRef = FirebaseDatabase.getInstance().getReference("users")
                .child("farmers")
                .child(currentUID);

        // Initialize views
        fullNameInput = findViewById(R.id.fullNameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveButton = findViewById(R.id.saveButton);
        mapSelection = findViewById(R.id.mapSelection);

        saveButton.setOnClickListener(v -> saveUserDetails());
        mapSelection.setOnClickListener(v -> {
            Intent intent = new Intent(this, FarmerMapPicker.class);
            startActivityForResult(intent, 1001);
        });

        loadUserDetails();
    }

    private void loadUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fullNameInput.setText(snapshot.child("username").getValue(String.class));
                    phoneInput.setText(snapshot.child("phone_number").getValue(String.class));
                    addressInput.setText(snapshot.child("address").getValue(String.class));
                } else {
                    Toast.makeText(Farmers_Details.this,
                            "User details not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Farmers_Details.this,
                        "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDetails() {
        String name = fullNameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("address", address);
        updates.put("phone_number", phone);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Farmers_Details.this, Home.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to update details", Toast.LENGTH_SHORT).show();
            }
        });
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
                List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
                if (!addressList.isEmpty()) {
                    Address addressObj = addressList.get(0);
                    String barangay = addressObj.getSubLocality();
                    String streetName = addressObj.getThoroughfare();
                    String city = addressObj.getLocality();

                    String fullAddress = (streetName != null ? streetName : "") + ", " +
                            (barangay != null ? barangay : "") + ", " +
                            (city != null ? city : "");

                    addressInput.setText(fullAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
