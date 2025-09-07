package com.example.loginappclone;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accepted_products);

        approvedOffersListView = findViewById(R.id.approvedOffersListView);
        noOffersMessage = findViewById(R.id.noOffersMessage);
        approvedOfferList = new ArrayList<>();

        dbRef = FirebaseDatabase.getInstance().getReference("requests");
        currentVendorUID = FirebaseAuth.getInstance().getCurrentUser().getUid(); // vendor UID

        setupAdapter();
        loadApprovedOffers(); // Start listening for Firebase updates
    }

    private void loadApprovedOffers() {
        dbRef.child(currentVendorUID)
                .addValueEventListener(new ValueEventListener() {  // 🔄 realtime listener
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        approvedOfferList.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot offerSnap : snapshot.getChildren()) {
                                String status = offerSnap.child("status").getValue(String.class);

                                if ("Accepted".equalsIgnoreCase(status)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("id", offerSnap.getKey()); // Firebase push ID
                                    map.put("farmer", "👨‍🌾 " + offerSnap.child("farmerName").getValue(String.class));
                                    map.put("product", "Product: " + offerSnap.child("productName").getValue(String.class));
                                    map.put("price", "Price: ₱" + offerSnap.child("price").getValue(Integer.class) + " / kilo");
                                    map.put("quantity", "Quantity: " + offerSnap.child("quantity").getValue(Integer.class) + " kilos");
                                    map.put("delivery", "Delivery: " + offerSnap.child("deliveryDate").getValue(String.class));
                                    map.put("status", status);

                                    approvedOfferList.add(map);
                                }
                            }

                            adapter.notifyDataSetChanged();

                            // ✅ Show "No Accepted Products" if none were found
                            if (approvedOfferList.isEmpty()) {
                                noOffersMessage.setText("No Accepted Products at the moment.");
                                noOffersMessage.setVisibility(View.VISIBLE);
                            } else {
                                noOffersMessage.setVisibility(View.GONE);
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
