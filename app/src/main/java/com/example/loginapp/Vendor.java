package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class Vendor extends AppCompatActivity {

    TextView welcomeText;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor);

        welcomeText = findViewById(R.id.welcomeText);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Get vendor name
        String username = sharedPreferences.getString("username", "Vendor");

        // Menu
        ImageView profile = findViewById(R.id.profile);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        profile.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END);
        });

        NavigationView navigationView = findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if(id == R.id.nav_displayedProduct) {
                Intent intent = new Intent(Vendor.this, VendorProducts.class);
                startActivity(intent);
                return true;
            }else if(id == R.id.nav_logout) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(Vendor.this, MainActivity.class);
                startActivity(intent);

                return true;
            }

            return false;
        });

        // Nav username
        TextView navUsername = navigationView.getHeaderView(0).findViewById(R.id.nav_username);
        navUsername.setText(username);
        
        welcomeText.setText("Welcome, " + username + "!");

        //CardViews
        CardView cardViewRequest = findViewById(R.id.cardViewRequest);
        CardView cardViewUpdateInfo = findViewById(R.id.cardViewUpdateInfo);
        CardView cardViewAcceptedOffers = findViewById(R.id.cardViewAcceptedOffers);
        CardView cardViewUpdateProduct = findViewById(R.id.cardViewUpdateProduct);


        // View Request
        cardViewRequest.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor.this, View_Offers.class);
            startActivity(intent);
        });

        // Update Info
        cardViewUpdateInfo.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor.this, Vendor_Info.class);
            startActivity(intent);
        });

        // Accepted Offers
        cardViewAcceptedOffers.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor.this, Accepted_Products.class);
            startActivity(intent);
        });

        // Update Product
        cardViewUpdateProduct.setOnClickListener(v ->{
            Intent intent = new Intent(Vendor.this, AddProducts.class);
            startActivity(intent);
        });
    }
}
