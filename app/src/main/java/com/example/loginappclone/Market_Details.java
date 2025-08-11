package com.example.loginappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;

public class Market_Details extends AppCompatActivity {
    String vendorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market_details); // ← this loads the layout!

        String marketName = getIntent().getStringExtra("marketName");
        vendorName = getIntent().getStringExtra("vendorName");
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

        // Set sold product by vendor
        LinearLayout productList = findViewById(R.id.vendor_products_list);
        MyDatabaseHelper db = new MyDatabaseHelper(this);
        int vendorId_int = Integer.parseInt(vendor_id);

        Cursor cursor = db.getVendorProductsSold(vendorId_int);

        if(cursor != null && cursor.moveToFirst()){
            do {
                String productName = cursor.getString(cursor.getColumnIndexOrThrow("vendor_product_name"));
                String productPrice = cursor.getString(cursor.getColumnIndexOrThrow("vendor_product_price"));
                String productUnit = cursor.getString(cursor.getColumnIndexOrThrow("product_unit"));

                LinearLayout rowLayout = new LinearLayout(this);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0,0,0,12);
                rowLayout.setLayoutParams(rowParams);

                // Product Name
                TextView product_name = new TextView(this);
                LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
                product_name.setLayoutParams(nameParams);
                product_name.setText(productName);
                product_name.setTextColor(getResources().getColor(R.color.white));

                // Product Price + Unit
                TextView priceView = new TextView(this);
                priceView.setText("₱" + productPrice + "/" + productUnit);
                priceView.setTextColor(getResources().getColor(R.color.white));

                // Add to row
                rowLayout.addView(product_name);
                rowLayout.addView(priceView);

                // Insert above the button
                productList.addView(rowLayout, productList.getChildCount() - 1);


            }while(cursor.moveToNext());
            cursor.close();
        }

        // Chat button

        Button chatButton = findViewById(R.id.chat_button);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Market_Details.this, Chat.class);
                intent.putExtra("chatWith", vendorName);
                startActivity(intent);

            }
        });

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