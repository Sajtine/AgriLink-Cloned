package com.example.loginappclone;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
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

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmers_details);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users")
                .child("farmers")
                .child(auth.getCurrentUser().getUid());

        // Initialize views
        fullNameInput = findViewById(R.id.fullNameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        saveButton = findViewById(R.id.saveButton);
        mapSelection = findViewById(R.id.mapSelection);

        saveButton.setOnClickListener(v -> saveUserDetails());
        mapSelection.setOnClickListener(v -> {
            Intent intent = new Intent(this, FarmerMapPicker.class);
            startActivityForResult(intent, 1001);
        });

        getFarmersDetails();
    }

    private void getFarmersDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);

                    fullNameInput.setText(name != null ? name : "");
                    emailInput.setText(email != null ? email : "");
                    phoneInput.setText(phoneNumber != null ? phoneNumber : "");
                    addressInput.setText(address != null ? address : "");

                    // If address or phone number is missing, prompt to fill
                    if (address == null || address.isEmpty() ||
                            phoneNumber == null || phoneNumber.isEmpty()) {
                        Toast.makeText(Farmers_Details.this,
                                "Please complete your profile with address and phone number.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Farmers_Details.this,
                            "User details not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Farmers_Details.this,
                        "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDetails() {
        String name = fullNameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("address", address);
        updates.put("phoneNumber", phone);
        updates.put("email", email);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show();

                boolean shouldDirectToMarket = getIntent().getBooleanExtra("redirectToMarket", false);
                if (shouldDirectToMarket) {
                    startActivity(new Intent(Farmers_Details.this, Market_Location.class));
                }

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
                Address addressObj = addressList.get(0);

                String barangay = addressObj.getSubLocality();
                String streetName = addressObj.getThoroughfare();
                String city = addressObj.getLocality();

                String fullAddress = (streetName != null ? streetName : "") + ", " +
                        (barangay != null ? barangay : "") + ", " +
                        (city != null ? city : "");

                addressInput.setText(fullAddress);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
