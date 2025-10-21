package com.example.loginappclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Market_Details extends AppCompatActivity {

    String vendorUID;
    String vendorName, marketName, barangay;
    DatabaseReference databaseRef;
    ImageView callFunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market_details);

        // Get UID from intent
        vendorUID = getIntent().getStringExtra("vendorUID");

        if (vendorUID == null || vendorUID.isEmpty()) {
            Toast.makeText(this, "Vendor ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Views
        TextView marketname = findViewById(R.id.marketname);
        TextView vendorname = findViewById(R.id.vendorname);
        TextView baranggay = findViewById(R.id.baranggay);
        TextView phoneNumber = findViewById(R.id.number);
        LinearLayout productList = findViewById(R.id.vendor_products_list);

        callFunction = findViewById(R.id.callFunction);

        // Call Function
        databaseRef.child("users").child("vendors").child(vendorUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    String phoneNumber = snapshot.child("phone_number").getValue(String.class);

                    String formattedNumber;

                    assert phoneNumber != null;
                    if (phoneNumber.startsWith("+63")) {
                        formattedNumber = "0" + phoneNumber.substring(3);
                    } else if (!phoneNumber.startsWith("0")) {
                        formattedNumber = "0" + phoneNumber;
                    } else {
                        formattedNumber = phoneNumber;
                    }

                    String finalFormattedNumber = formattedNumber;
                    callFunction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            makeCall(finalFormattedNumber);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Load Market Info in market node
        databaseRef.child("markets").child(vendorUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            marketName = snapshot.child("marketName").getValue(String.class);
                            barangay = snapshot.child("barangay").getValue(String.class);

                            marketname.setText(marketName);
                            baranggay.setText(barangay);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Load vendor username and phone number
        databaseRef.child("users").child("vendors").child(vendorUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            vendorName = snapshot.child("username").getValue(String.class);

                            String phone = snapshot.child("phone_number").getValue(String.class);

                            String formatted_phoneNumber;

                            assert phone != null;
                            if (phone.startsWith("+63")) {
                                formatted_phoneNumber = "0" + phone.substring(3);
                            } else if (!phone.startsWith("0")) {
                                formatted_phoneNumber = "0" + phone;
                            } else {
                                formatted_phoneNumber = phone;
                            }

                            phoneNumber.setText(formatted_phoneNumber);
                            vendorname.setText(vendorName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


        // Load Vendor Products
        databaseRef.child("vendor_products").child(vendorUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.removeAllViews();

                        for (DataSnapshot productSnap : snapshot.getChildren()) {
                            String productName = productSnap.child("vendor_product_name").getValue(String.class);

                            Integer productPriceInt = productSnap.child("vendor_product_price").getValue(Integer.class);
                            String productPrice = String.valueOf(productPriceInt);

                            String productUnit = productSnap.child("product_unit").getValue(String.class);

                            LinearLayout rowLayout = new LinearLayout(Market_Details.this);
                            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                            rowLayout.setGravity(Gravity.CENTER_VERTICAL);
                            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            rowParams.setMargins(0, 0, 0, 12);
                            rowLayout.setLayoutParams(rowParams);

                            // Product Name
                            TextView product_name = new TextView(Market_Details.this);
                            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1
                            );
                            product_name.setLayoutParams(nameParams);
                            product_name.setText(productName);
                            product_name.setTextSize(16);
                            product_name.setTextColor(getResources().getColor(R.color.white));

                            // Product Price + Unit
                            TextView priceView = new TextView(Market_Details.this);
                            priceView.setText("₱" + productPrice + "/" + productUnit);
                            priceView.setTextColor(getResources().getColor(R.color.white));

                            // Add to row
                            rowLayout.addView(product_name);
                            rowLayout.addView(priceView);

                            // Add to list
                            productList.addView(rowLayout);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Chat button
        Button chatButton = findViewById(R.id.chat_button);
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(Market_Details.this, Chat.class);
            intent.putExtra("chatWithUID", vendorUID);
            startActivity(intent);
        });

        // Request Sell Button
        Button btnRequest = findViewById(R.id.request_sell);
        btnRequest.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("vendor_id", vendorUID);
            bundle.putString("marketName", marketName);
            bundle.putString("vendorName", vendorName);

            Request_Sell fragment = new Request_Sell();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), "Request_Sell");
        });

        // Bottom Navigation
        findViewById(R.id.location).setOnClickListener(v ->
                startActivity(new Intent(Market_Details.this, Market_Location.class))
        );

        findViewById(R.id.home).setOnClickListener(v ->
                startActivity(new Intent(Market_Details.this, Home.class))
        );

        findViewById(R.id.user_profile).setOnClickListener(v ->
                startActivity(new Intent(Market_Details.this, Profile.class))
        );
    }

    // Call method
    private void makeCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));

        // Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }

        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can retry the call
                Toast.makeText(this, "Permission granted, tap again to call", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
