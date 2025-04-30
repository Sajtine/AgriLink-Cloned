package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Vendor extends AppCompatActivity {

    TextView welcomeText;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor);

        welcomeText = findViewById(R.id.welcomeText);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "Vendor");

        welcomeText.setText("Welcome, Vendor!");

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(Vendor.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Button viewProductsButton = findViewById(R.id.viewProductsButton);
        viewProductsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor.this, View_Offers.class);
            startActivity(intent);
        });

        Button updateInfoButton = findViewById(R.id.updateInfoButton);
        updateInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Vendor.this, Vendor_Info.class);
                startActivity(intent);
            }
        });

        Button viewApprovedOffersButton = findViewById(R.id.viewApprovedOffersButton);
        viewApprovedOffersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Vendor.this, Accepted_Products.class));
            }
        });


    }
}