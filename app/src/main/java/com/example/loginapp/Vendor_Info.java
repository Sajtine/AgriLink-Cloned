package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Vendor_Info extends AppCompatActivity {

    EditText MarketName, Street, Barangay, PhoneNumber, Municipality;
    Button btnUpdateMarket, openMap;
    MyDatabaseHelper databaseHelper;
    SharedPreferences sharedPreferences;
    int vendorId;

    String latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor_info);

        // Init views
        MarketName = findViewById(R.id.MarketName);
        Street = findViewById(R.id.Street);
        Barangay = findViewById(R.id.Barangay);
        PhoneNumber = findViewById(R.id.PhoneNumber);
        Municipality = findViewById(R.id.Municipality);
        btnUpdateMarket = findViewById(R.id.btnUpdateMarket);
        openMap = findViewById(R.id.openMap);

        // Init database and shared preferences
        databaseHelper = new MyDatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        vendorId = Integer.parseInt(sharedPreferences.getString("userId", "0"));

        // Optional: Load existing data here from DB if needed
        Cursor cursor = databaseHelper.getMarketInfoByVendorId(vendorId);

        if (cursor.moveToFirst()) {
            MarketName.setText(cursor.getString(0));
            Street.setText(cursor.getString(1));
            Barangay.setText(cursor.getString(2));
            PhoneNumber.setText(cursor.getString(3));
            Municipality.setText(cursor.getString(4));
        }


        cursor.close();

        btnUpdateMarket.setOnClickListener(v -> {
            String name = MarketName.getText().toString().trim();
            String street = Street.getText().toString().trim();
            String barangay = Barangay.getText().toString().trim();
            String phone = PhoneNumber.getText().toString().trim();
            String municipality = Municipality.getText().toString().trim();
            String lat = latitude;
            String lng = longitude;

            if (name.isEmpty() || street.isEmpty() || barangay.isEmpty() || phone.isEmpty() ||
                    municipality.isEmpty() || longitude.isEmpty() || latitude.isEmpty()) {
                Toast.makeText(Vendor_Info.this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = databaseHelper.updateMarketInfo(
                    vendorId, name, street, barangay, phone, municipality, lng, lat
            );

            if (success) {
                Toast.makeText(Vendor_Info.this, "Market info updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(Vendor_Info.this, "Failed to update market info.", Toast.LENGTH_SHORT).show();
            }
        });

        // check info if completed
        boolean infoCheck = getIntent().getBooleanExtra("info_complete", true);
        if (!infoCheck) {
            showInfoDialog();
        }

        // open map
        openMap.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor_Info.this, MapPicker.class);
            startActivityForResult(intent, 1001);
        });


    }

    // note for vendors
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
                List<Address> adddressList = geocoder.getFromLocation(lat, lng, 1);
                Address address = adddressList.get(0);

                String barangay = address.getSubLocality();
                String streetName = address.getThoroughfare();
                String city = address.getLocality();

                Barangay.setText(barangay);
                Street.setText(streetName);
                Municipality.setText(city);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
            }
        }
    }
}