package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Vendor_Info extends AppCompatActivity {

    EditText MarketName, Street, Barangay, PhoneNumber, Municipality, Longitude, Latitude;
    Button btnUpdateMarket;
    MyDatabaseHelper databaseHelper;
    SharedPreferences sharedPreferences;
    int vendorId;

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
        Longitude = findViewById(R.id.Longitude);
        Latitude = findViewById(R.id.Latitude);
        btnUpdateMarket = findViewById(R.id.btnUpdateMarket);

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
            Longitude.setText(cursor.getString(5));
            Latitude.setText(cursor.getString(6));
        }


        cursor.close();

        btnUpdateMarket.setOnClickListener(v -> {
            String name = MarketName.getText().toString().trim();
            String street = Street.getText().toString().trim();
            String barangay = Barangay.getText().toString().trim();
            String phone = PhoneNumber.getText().toString().trim();
            String municipality = Municipality.getText().toString().trim();
            String longitude = Longitude.getText().toString().trim();
            String latitude = Latitude.getText().toString().trim();

            if (name.isEmpty() || street.isEmpty() || barangay.isEmpty() || phone.isEmpty() ||
                    municipality.isEmpty() || longitude.isEmpty() || latitude.isEmpty()) {
                Toast.makeText(Vendor_Info.this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = databaseHelper.updateMarketInfo(
                    vendorId, name, street, barangay, phone, municipality, longitude, latitude
            );

            if (success) {
                Toast.makeText(Vendor_Info.this, "Market info updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(Vendor_Info.this, "Failed to update market info.", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnAddProducts = findViewById(R.id.btnAddProducts);

       btnAddProducts.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {
               startActivity(new Intent(Vendor_Info.this, AddProducts.class));
           }
       });

    }
}