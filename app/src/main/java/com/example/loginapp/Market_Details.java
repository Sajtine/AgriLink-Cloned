package com.example.loginapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Market_Details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market_details); // ← this loads the layout!

        String marketName = getIntent().getStringExtra("marketName");
        String vendorName = getIntent().getStringExtra("vendorName");
        String vendor_id = getIntent().getStringExtra("vendor_id");
        String barangay = getIntent().getStringExtra("barangay");
        String phone_number = getIntent().getStringExtra("phone");


        TextView marketname = findViewById(R.id.marketname);
        TextView vendorname = findViewById(R.id.vendorname);
        TextView baranggay = findViewById(R.id.baranggay);
        TextView phoneNumber = findViewById(R.id.number);

        marketname.setText(marketName);
        vendorname.setText(vendorName);
        baranggay.setText(barangay);
        phoneNumber.setText(phone_number);


        Button btnRequest = findViewById(R.id.request_sell);
        btnRequest.setOnClickListener(view -> {

            Request_Sell fragment = Request_Sell.newInstance(vendor_id); // ← pass it here
            fragment.show(getSupportFragmentManager(), "Request_Sell");
        });


        ImageView loc = findViewById(R.id.location);
        loc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(Market_Details.this, Market_Location.class));
            }
        });

        ImageView home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Market_Details.this, Home.class));
            }
        });

        ImageView profile = findViewById(R.id.user_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Market_Details.this, Profile.class));
            }
        });



    }

}