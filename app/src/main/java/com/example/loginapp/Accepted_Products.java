package com.example.loginapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Accepted_Products extends AppCompatActivity {

    ListView approvedOffersListView;
    TextView noOffersMessage;
    ArrayList<HashMap<String, String>> approvedOfferList;
    MyDatabaseHelper dbHelper;
    boolean hasApprovedOffers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accepted_products);

        approvedOffersListView = findViewById(R.id.approvedOffersListView);
        noOffersMessage = findViewById(R.id.noOffersMessage); // Reference to the message view
        dbHelper = new MyDatabaseHelper(this);
        approvedOfferList = new ArrayList<>();

        loadApprovedOffers();

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                approvedOfferList,
                R.layout.approved_offer_item,
                new String[]{"farmer", "product", "price", "quantity", "delivery", "status"},
                new int[]{R.id.farmerName, R.id.productName, R.id.productPrice, R.id.productQuantity, R.id.deliveryDate, R.id.offerStatus}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Button btnReceived = view.findViewById(R.id.btnReceived);
                TextView statusView = view.findViewById(R.id.offerStatus);

                String status = approvedOfferList.get(position).get("status");

                if ("Received".equalsIgnoreCase(status)) {
                    btnReceived.setVisibility(View.GONE);
                } else {
                    btnReceived.setVisibility(View.VISIBLE);
                }

                if (statusView != null) {
                    statusView.setText("Status: " + status);
                    if ("Received".equalsIgnoreCase(status)) {
                        statusView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    } else {
                        statusView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                }

                btnReceived.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String productName = approvedOfferList.get(position).get("product");
                        updateOfferStatusToReceived(position);
                        loadApprovedOffers(); // Refresh data
                        ((SimpleAdapter) approvedOffersListView.getAdapter()).notifyDataSetChanged(); // Refresh UI
                        Toast.makeText(Accepted_Products.this, "Offer marked as received for " + productName, Toast.LENGTH_SHORT).show();
                    }
                });

                return view;
            }
        };

        approvedOffersListView.setAdapter(adapter);
    }

    private void loadApprovedOffers() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int vendorId = Integer.parseInt(sharedPreferences.getString("userId", "0"));

        Cursor cursor = dbHelper.getApprovedOffersByVendor(vendorId);
        approvedOfferList.clear();
        hasApprovedOffers = false;

        if (cursor != null && cursor.moveToFirst()) {
            hasApprovedOffers = true;
            noOffersMessage.setVisibility(View.GONE); // Hide the "no offers" message

            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))));
                map.put("farmer", "👨‍🌾 " + cursor.getString(cursor.getColumnIndexOrThrow("farmer_name")));
                map.put("product", "Product: " + cursor.getString(cursor.getColumnIndexOrThrow("product_name")));
                map.put("price", "Price: ₱" + cursor.getString(cursor.getColumnIndexOrThrow("price")) + " / kilo");
                map.put("quantity", "Quantity: " + cursor.getString(cursor.getColumnIndexOrThrow("quantity")) + " kilos");
                map.put("delivery", "Delivery: " + cursor.getString(cursor.getColumnIndexOrThrow("delivery_date")));
                map.put("status", cursor.getString(cursor.getColumnIndexOrThrow("status")));
                approvedOfferList.add(map);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            noOffersMessage.setVisibility(View.VISIBLE); // Show "no offers" message
        }
    }

    private void updateOfferStatusToReceived(int position) {
        int offerId = Integer.parseInt(approvedOfferList.get(position).get("id"));

        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        dbHelper.markOfferAsReceived(offerId, currentDate);
    }
}
