package com.example.loginappclone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class Product_History extends AppCompatActivity {

    ListView productHistoryListView;
    ArrayList<HashMap<String, String>> historyList;
    TextView noHistoryMessage;
    Button back;

    DatabaseReference requestsRef;
    String vendorUID;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_history);

        productHistoryListView = findViewById(R.id.product_historyListView);
        historyList = new ArrayList<>();
        noHistoryMessage = findViewById(R.id.noHistoryMessage);

        back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());

        // Get vendor UID (current logged-in Firebase user)
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        vendorUID = sharedPreferences.getString("uid", null);
        requestsRef = FirebaseDatabase.getInstance().getReference("requests").child(vendorUID);

        loadReceivedProductHistory();
    }

    private void loadReceivedProductHistory() {
        historyList.clear();

        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot reqSnapshot : snapshot.getChildren()) {
                        String status = reqSnapshot.child("status").getValue(String.class);
                        if (status == null || !status.equalsIgnoreCase("Received")) {
                            // Skip requests that are not received
                            continue;
                        }

                        String farmerName = reqSnapshot.child("farmerName").getValue(String.class);
                        String productName = reqSnapshot.child("productName").getValue(String.class);

                        // Fetch price
                        Long priceLong = reqSnapshot.child("price").getValue(Long.class);
                        int priceInt = (priceLong != null) ? priceLong.intValue() : 0;
                        String price = String.valueOf(priceInt);

                        // Fetch quantity
                        Long qtyLong = reqSnapshot.child("quantity").getValue(Long.class);
                        int qtyInt = (qtyLong != null) ? qtyLong.intValue() : 0;
                        String quantity = String.valueOf(qtyInt);

                        String receivedDate = reqSnapshot.child("receivedDate").getValue(String.class);
                        if (receivedDate == null) receivedDate = "N/A";

                        HashMap<String, String> map = new HashMap<>();
                        map.put("farmer", "👨‍🌾 " + (farmerName != null ? farmerName : "Unknown"));
                        map.put("product", "Product: " + (productName != null ? productName : ""));
                        map.put("price", "Price: ₱" + price + " / kilos");
                        map.put("quantity", "Quantity: " + quantity + " kilos");
                        map.put("status", "Status: " + status);
                        map.put("received_date", "Received: " + receivedDate);

                        historyList.add(map);
                    }

                    if (historyList.isEmpty()) {
                        noHistoryMessage.setVisibility(View.VISIBLE);
                        noHistoryMessage.setText("No received products yet.");
                    } else {
                        noHistoryMessage.setVisibility(View.GONE);
                        SimpleAdapter adapter = new SimpleAdapter(
                                Product_History.this,
                                historyList,
                                R.layout.product_history_card,
                                new String[]{"farmer", "product", "price", "quantity", "status", "received_date"},
                                new int[]{R.id.farmerName, R.id.productName, R.id.productPrice, R.id.productQuantity, R.id.status, R.id.receivedDate}
                        );
                        productHistoryListView.setAdapter(adapter);
                    }
                } else {
                    noHistoryMessage.setVisibility(View.VISIBLE);
                    noHistoryMessage.setText("No received products yet.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                noHistoryMessage.setVisibility(View.VISIBLE);
                noHistoryMessage.setText("Error loading history: " + error.getMessage());
            }
        });
    }
}
