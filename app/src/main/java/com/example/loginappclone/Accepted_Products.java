package com.example.loginappclone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Accepted_Products extends AppCompatActivity {

    ListView approvedOffersListView;
    TextView noOffersMessage;
    ArrayList<HashMap<String, String>> approvedOfferList;
    DatabaseReference dbRef;
    String currentVendorUID;
    SimpleAdapter adapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accepted_products);

        approvedOffersListView = findViewById(R.id.approvedOffersListView);
        noOffersMessage = findViewById(R.id.noOffersMessage);
        approvedOfferList = new ArrayList<>();

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        dbRef = FirebaseDatabase.getInstance().getReference("requests");
        currentVendorUID = sharedPreferences.getString("uid", null); // vendor UID

        setupAdapter();
        loadApprovedOffers();
    }

    private void loadApprovedOffers() {
        dbRef.child(currentVendorUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        approvedOfferList.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot offerSnap : snapshot.getChildren()) {
                                String status = offerSnap.child("status").getValue(String.class);

                                if ("Accepted".equalsIgnoreCase(status)) {
                                    String farmerUID = offerSnap.child("farmerUID").getValue(String.class);
                                    String productName = offerSnap.child("productName").getValue(String.class);
                                    Integer price = offerSnap.child("price").getValue(Integer.class);
                                    Integer quantity = offerSnap.child("quantity").getValue(Integer.class);
                                    String deliveryDate = offerSnap.child("deliveryDate").getValue(String.class);

                                    if (farmerUID != null) {
                                        DatabaseReference farmerRef = FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child("farmers")
                                                .child(farmerUID);

                                        farmerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot farmerSnap) {
                                                String farmerName = farmerSnap.child("username").getValue(String.class);
                                                if (farmerName == null) farmerName = "Unknown Farmer";

                                                HashMap<String, String> map = new HashMap<>();
                                                map.put("id", offerSnap.getKey());
                                                map.put("farmer", "👨‍🌾 " + farmerName);
                                                map.put("product", "Product: " + productName);
                                                map.put("price", "Price: ₱" + price + " / kilo");
                                                map.put("quantity", "Quantity: " + quantity + " kilos");
                                                map.put("delivery", "Delivery: " + deliveryDate);
                                                map.put("status", status);

                                                approvedOfferList.add(map);
                                                adapter.notifyDataSetChanged();

                                                // Hide "no offers" message if there are items
                                                noOffersMessage.setVisibility(
                                                        approvedOfferList.isEmpty() ? View.VISIBLE : View.GONE
                                                );
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(Accepted_Products.this, "Error loading farmer: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }

                            if (approvedOfferList.isEmpty()) {
                                noOffersMessage.setText("No Accepted Products at the moment.");
                                noOffersMessage.setVisibility(View.VISIBLE);
                            }

                        } else {
                            noOffersMessage.setText("No Accepted Products at the moment.");
                            noOffersMessage.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Accepted_Products.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setupAdapter() {
        adapter = new SimpleAdapter(
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

                btnReceived.setOnClickListener(v -> {
                    String offerId = approvedOfferList.get(position).get("id");
                    String productName = approvedOfferList.get(position).get("product");

                    updateOfferStatusToReceived(offerId);
                    Toast.makeText(Accepted_Products.this, "Offer marked as received for " + productName, Toast.LENGTH_SHORT).show();
                });

                return view;
            }
        };

        approvedOffersListView.setAdapter(adapter);
    }

    private void updateOfferStatusToReceived(String offerId) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        dbRef.child(currentVendorUID).child(offerId).child("status").setValue("Received");
        dbRef.child(currentVendorUID).child(offerId).child("receivedDate").setValue(currentDate);
    }   
}
