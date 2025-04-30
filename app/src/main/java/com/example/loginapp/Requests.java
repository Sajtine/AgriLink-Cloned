package com.example.loginapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class Requests extends AppCompatActivity {

    TextView btnPending, btnApproved, tabDelivered, tabDeclined;
    ListView listView;
    MyDatabaseHelper dbHelper;
    SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        btnPending = findViewById(R.id.tabPending);
        btnApproved = findViewById(R.id.tabApproved);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabDeclined = findViewById(R.id.tabDeclined);
        listView = findViewById(R.id.listView);
        dbHelper = new MyDatabaseHelper(this);

        // Load "Pending" tab by default
        highlightTab("Pending");
        loadData("Pending");

        btnPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightTab("Pending");
                loadData("Pending");
            }
        });

        btnApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightTab("Approved");
                loadData("Approved");
            }
        });

        tabDeclined.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                highlightTab("Declined");
                loadData("Declined");
            }
        });

        tabDelivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightTab("Received");
                loadData("Received");
            }
        });
    }

    // Method to load the data into the ListView
    private void loadData(String status) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int farmerId = Integer.parseInt(sharedPreferences.getString("userId", "0"));

        ArrayList<HashMap<String, String>> offerList;

        switch (status) {
            case "Pending":
                offerList = dbHelper.getProductOffersByStatus("Pending", farmerId);
                break;
            case "Received":
                offerList = dbHelper.getProductOffersByStatus("Received", farmerId);
                break;
            case "Declined":
                offerList = dbHelper.getProductOffersByStatus("Declined", farmerId);
                break;
            default:
                offerList = dbHelper.getProductOffersByStatus("Accepted", farmerId); // Approved = Accepted in DB
                break;
        }

        ArrayList<HashMap<String, String>> displayData = new ArrayList<>();
        for (HashMap<String, String> offer : offerList) {
            HashMap<String, String> map = new HashMap<>();
            map.put("product_name", offer.get("product_name"));
            map.put("status", "Status: " + offer.get("status"));
            map.put("market_name", "Market: " + offer.get("market_name"));
            map.put("vendor_barangay", "Barangay: " + offer.get("vendor_barangay"));
            map.put("delivery_date", "Delivery Date: " + offer.get("delivery_date"));
            displayData.add(map);
        }

        if (displayData.isEmpty()) {
            HashMap<String, String> emptyMap = new HashMap<>();
            emptyMap.put("product_name", "No " + status.toLowerCase() + " requests found.");
            emptyMap.put("status", "");
            emptyMap.put("market_name", "");
            emptyMap.put("vendor_barangay", "");
            emptyMap.put("delivery_date", "");
            displayData.add(emptyMap);
        }

        adapter = new SimpleAdapter(
                this,
                displayData,
                R.layout.list_item_request,
                new String[]{"product_name", "status", "market_name", "vendor_barangay", "delivery_date"},
                new int[]{R.id.textProductName, R.id.textStatus, R.id.textMarket, R.id.textBarangay, R.id.delivery_date}
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
            case "Approved":
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
