package com.example.loginappclone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Requests extends AppCompatActivity {

    TextView btnPending, btnApproved, tabDelivered, tabDeclined;
    ListView listView;
    SimpleAdapter adapter;

    DatabaseReference requestsRef, marketsRef;
    String currentUserUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        btnPending = findViewById(R.id.tabPending);
        btnApproved = findViewById(R.id.tabApproved);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabDeclined = findViewById(R.id.tabDeclined);
        listView = findViewById(R.id.listView);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserUID = sharedPreferences.getString("uid", null);
        requestsRef = FirebaseDatabase.getInstance().getReference("requests");
        marketsRef = FirebaseDatabase.getInstance().getReference("markets");

        // Load "Pending" tab by default
        highlightTab("Pending");
        loadData("Pending");

        btnPending.setOnClickListener(v -> {
            highlightTab("Pending");
            loadData("Pending");
        });

        btnApproved.setOnClickListener(v -> {
            highlightTab("Accepted");
            loadData("Accepted");
        });

        tabDeclined.setOnClickListener(v -> {
            highlightTab("Declined");
            loadData("Declined");
        });

        tabDelivered.setOnClickListener(v -> {
            highlightTab("Received");
            loadData("Received");
        });
    }

    // Load data from Firebase
    private void loadData(String statusFilter) {
        ArrayList<HashMap<String, String>> displayData = new ArrayList<>();

        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                displayData.clear();

                boolean foundData = false;

                for (DataSnapshot vendorSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot requestSnap : vendorSnapshot.getChildren()) {
                        String buyerUID = requestSnap.child("farmerUID").getValue(String.class);
                        String status = requestSnap.child("status").getValue(String.class);

                        if (buyerUID != null && buyerUID.equals(currentUserUID)) {
                            if (status != null && status.equalsIgnoreCase(statusFilter)) {
                                foundData = true;

                                String productName = requestSnap.child("productName").getValue(String.class);
                                String deliveryDate = requestSnap.child("deliveryDate").getValue(String.class);
                                String requestDate = requestSnap.child("requestDate").getValue(String.class);
                                String vendorUID = requestSnap.child("vendorUID").getValue(String.class);

                                Integer priceInt = requestSnap.child("price").getValue(Integer.class);
                                String price = (priceInt != null ? String.valueOf(priceInt) : "0") + " / kg";

                                Integer quantityInt = requestSnap.child("quantity").getValue(Integer.class);
                                String quantity = (quantityInt != null ? "Quantity: " + quantityInt + " kg" : "Quantity: 0 kg");

                                HashMap<String, String> map = new HashMap<>();
                                map.put("product_name", productName != null ? productName : "N/A");
                                map.put("status", "Status: " + status);
                                map.put("delivery_date", "Delivery Date: " + (deliveryDate != null ? deliveryDate : ""));
                                map.put("request_date", "Request Date: " + (requestDate != null ? requestDate : ""));
                                map.put("price", "Price: " + (price != null ? price : ""));
                                map.put("quantity", quantity);

                                if (vendorUID != null) {
                                    marketsRef.child(vendorUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot marketSnap) {
                                            String marketName = marketSnap.child("marketName").getValue(String.class);
                                            String barangay = marketSnap.child("barangay").getValue(String.class);

                                            map.put("market_name", "Market: " + (marketName != null ? marketName : "Unknown"));
                                            map.put("vendor_barangay", "Barangay: " + (barangay != null ? barangay : "Unknown"));

                                            displayData.add(map);
                                            updateListView(displayData);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(Requests.this, "Failed to load market info", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }

                // only add the "No requests" message if nothing matched
                if (!foundData) {
                    HashMap<String, String> emptyMap = new HashMap<>();
                    emptyMap.put("product_name", "No " + statusFilter.toLowerCase() + " requests found.");
                    emptyMap.put("status", "");
                    emptyMap.put("market_name", "");
                    emptyMap.put("vendor_barangay", "");
                    emptyMap.put("delivery_date", "");
                    emptyMap.put("request_date", "");
                    emptyMap.put("price", "");
                    emptyMap.put("quantity", "");
                    displayData.add(emptyMap);
                    updateListView(displayData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Requests.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateListView(ArrayList<HashMap<String, String>> displayData) {
        adapter = new SimpleAdapter(
                this,
                displayData,
                R.layout.list_item_request,
                new String[]{"product_name", "status", "price", "quantity", "market_name", "vendor_barangay", "delivery_date", "request_date"},
                new int[]{R.id.textProductName, R.id.textStatus, R.id.priceUnit, R.id.requestQuantity, R.id.textMarket, R.id.textBarangay, R.id.delivery_date, R.id.request_date}
        );

        listView.setAdapter(adapter);
    }

    // Method to visually highlight selected tab
    private void highlightTab(String selectedTab) {
        btnPending.setBackgroundResource(R.drawable.tab_unselected);
        btnApproved.setBackgroundResource(R.drawable.tab_unselected);
        tabDeclined.setBackgroundResource(R.drawable.tab_unselected);
        tabDelivered.setBackgroundResource(R.drawable.tab_unselected);

        btnPending.setTextColor(getResources().getColor(android.R.color.black));
        btnApproved.setTextColor(getResources().getColor(android.R.color.black));
        tabDeclined.setTextColor(getResources().getColor(android.R.color.black));
        tabDelivered.setTextColor(getResources().getColor(android.R.color.black));

        switch (selectedTab) {
            case "Pending":
                btnPending.setBackgroundResource(R.drawable.tab_selected);
                btnPending.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "Accepted":
                btnApproved.setBackgroundResource(R.drawable.tab_selected);
                btnApproved.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "Declined":
                tabDeclined.setBackgroundResource(R.drawable.tab_selected);
                tabDeclined.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "Received":
                tabDelivered.setBackgroundResource(R.drawable.tab_selected);
                tabDelivered.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
    }
}
