package com.example.loginappclone;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class Vendor_Info extends AppCompatActivity {

    EditText MarketName, Street, Barangay, PhoneNumber, Municipality;
    Button btnUpdateMarket, openMap;

    String latitude = "";
    String longitude = "";

    FirebaseDatabase database;
    DatabaseReference marketRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor_info);

        // Initialize views
        MarketName = findViewById(R.id.MarketName);
        Street = findViewById(R.id.Street);
        Barangay = findViewById(R.id.Barangay);
        PhoneNumber = findViewById(R.id.PhoneNumber);
        Municipality = findViewById(R.id.Municipality);
        btnUpdateMarket = findViewById(R.id.btnUpdateMarket);
        openMap = findViewById(R.id.openMap);

        database = FirebaseDatabase.getInstance();

        // Get current Firebase user UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish(); // close this activity if no user logged in
            return;
        }
        String uid = currentUser.getUid();

        // Use UID as key in markets node
        marketRef = database.getReference("markets").child(uid);

        // Load existing market info from Firebase
        marketRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    MarketName.setText(snapshot.child("marketName").getValue(String.class));
                    Street.setText(snapshot.child("street").getValue(String.class));
                    Barangay.setText(snapshot.child("barangay").getValue(String.class));
                    PhoneNumber.setText(snapshot.child("phoneNumber").getValue(String.class));
                    Municipality.setText(snapshot.child("municipality").getValue(String.class));
                    latitude = snapshot.child("latitude").getValue(String.class);
                    longitude = snapshot.child("longitude").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Vendor_Info.this, "Failed to load market info.", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdateMarket.setOnClickListener(v -> {
            String name = MarketName.getText().toString().trim();
            String street = Street.getText().toString().trim();
            String barangay = Barangay.getText().toString().trim();
            String phone = PhoneNumber.getText().toString().trim();
            String municipality = Municipality.getText().toString().trim();

            if (name.isEmpty() || street.isEmpty() || barangay.isEmpty() || phone.isEmpty() ||
                    municipality.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(Vendor_Info.this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> marketInfo = new HashMap<>();
            marketInfo.put("marketName", name);
            marketInfo.put("street", street);
            marketInfo.put("barangay", barangay);
            marketInfo.put("phoneNumber", phone);
            marketInfo.put("municipality", municipality);
            marketInfo.put("latitude", latitude);
            marketInfo.put("longitude", longitude);
            marketInfo.put("infoComplete", true);

            marketRef.updateChildren(marketInfo).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Vendor_Info.this, "Market info updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Vendor_Info.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Vendor_Info.this, "Failed to update market info.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Check if info is completed (optional if you want to show dialog)
        boolean infoCheck = getIntent().getBooleanExtra("info_complete", true);
        if (!infoCheck) {
            showInfoDialog();
        }

        // Open map picker activity
        openMap.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor_Info.this, MapPicker.class);
            startActivityForResult(intent, 1001);
        });
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Important Information!")
                .setMessage("Welcome to AgriLink! Before you can access the main features, please complete your profile information. We assure you that your data will be kept private and secure.")
                .setPositiveButton("OK", null)
                .show();
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
                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);

                    String barangay = address.getSubLocality();
                    String streetName = address.getThoroughfare();
                    String city = address.getLocality();

                    Barangay.setText(barangay != null ? barangay : "");
                    Street.setText(streetName != null ? streetName : "");
                    Municipality.setText(city != null ? city : "");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
