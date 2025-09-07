package com.example.loginappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class View_Offers extends AppCompatActivity {

    ListView offersListView;
    Button backButton;
    ArrayList<String> offerList;
    ArrayList<String> offerIdList;
    DatabaseReference offersRef;
    String vendorUID, farmerName, farmerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_offers);

        offersListView = findViewById(R.id.offersListView);
        backButton = findViewById(R.id.backButton);

        vendorUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        offersRef = FirebaseDatabase.getInstance().getReference("requests").child(vendorUID);

        loadFarmerOffers();

        backButton.setOnClickListener(v -> finish());
    }

    private void loadFarmerOffers() {
        offerList = new ArrayList<>();
        offerIdList = new ArrayList<>();

        offersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                offerList.clear();
                offerIdList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot offerSnap : snapshot.getChildren()) {
                        String status = offerSnap.child("status").getValue(String.class);

                        if (status == null || status.equalsIgnoreCase("Pending")) {
                            String offerId = offerSnap.getKey();
                            farmerName = offerSnap.child("farmerName").getValue(String.class);
                            String farmerNumber = offerSnap.child("phoneNumber").getValue(String.class);
                            String product = offerSnap.child("productName").getValue(String.class);
                            Integer priceInt = offerSnap.child("price").getValue(Integer.class);
                            Integer quantityInt = offerSnap.child("quantity").getValue(Integer.class);
                            String deliveryDate = offerSnap.child("deliveryDate").getValue(String.class);
                            String pickUpOption = offerSnap.child("pickupOption").getValue(String.class);
                            farmerAddress = offerSnap.child("address").getValue(String.class);

                            String price = (priceInt != null) ? String.valueOf(priceInt) : "";
                            String quantity = (quantityInt != null) ? String.valueOf(quantityInt) : "";

                            String offerDetails = "👨‍🌾 " + farmerName +
                                    "\nProduct: " + product +
                                    "\nFarmer No. " + farmerNumber +
                                    "\nPrice: ₱" + price + " / kilo" +
                                    "\nQuantity: " + quantity + " kilos" +
                                    "\nDelivery Date: " + deliveryDate;

                            if ("Vendor will pick up the product".equalsIgnoreCase(pickUpOption)) {
                                offerDetails += "\nFarmer Location: " + (farmerAddress != null ? farmerAddress : "Not provided");
                            }

                            offerList.add(offerDetails);
                            offerIdList.add(offerId);
                        }
                    }
                }

                if (offerList.isEmpty()) {
                    offerList.add("No offers available.");
                }

                offersListView.setAdapter(new OfferAdapter());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Offers.this, "Failed to load offers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class OfferAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return offerList.size();
        }

        @Override
        public Object getItem(int position) {
            return offerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(View_Offers.this).inflate(R.layout.offers_item, parent, false);

            TextView txtOfferDetails = view.findViewById(R.id.txtOfferDetails);
            Button btnAccept = view.findViewById(R.id.btnAccept);
            Button btnDecline = view.findViewById(R.id.btnDecline);
            Button btnCheckLocation = view.findViewById(R.id.btnCheckLocation); // Add button in layout

            txtOfferDetails.setText(offerList.get(position));

            if (offerList.get(position).equals("No offers available.")) {
                btnAccept.setVisibility(View.GONE);
                btnDecline.setVisibility(View.GONE);
                btnCheckLocation.setVisibility(View.GONE);
            } else {
                btnAccept.setVisibility(View.VISIBLE);
                btnDecline.setVisibility(View.VISIBLE);

                // Show Check Location button only if Farmer Location exists
                if (offerList.get(position).contains("Farmer Location")) {
                    btnCheckLocation.setVisibility(View.VISIBLE);
                } else {
                    btnCheckLocation.setVisibility(View.GONE);
                }

                btnAccept.setOnClickListener(v -> {
                    String offerId = offerIdList.get(position);
                    offersRef.child(offerId).child("status").setValue("Accepted");
                    Toast.makeText(View_Offers.this, "Offer accepted!", Toast.LENGTH_SHORT).show();
                    offerList.remove(position);
                    offerIdList.remove(position);
                    notifyDataSetChanged();
                });

                btnDecline.setOnClickListener(v -> {
                    String offerId = offerIdList.get(position);
                    offersRef.child(offerId).child("status").setValue("Declined");
                    Toast.makeText(View_Offers.this, "Offer declined.", Toast.LENGTH_SHORT).show();
                    offerList.remove(position);
                    offerIdList.remove(position);
                    notifyDataSetChanged();
                });

                btnCheckLocation.setOnClickListener(v -> {
                    String offerId = offerIdList.get(position);

                    offersRef.child(offerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Double lat = snapshot.child("latitude").getValue(Double.class);
                            Double lng = snapshot.child("longitude").getValue(Double.class);

                            if (lat != null && lng != null) {
                                Intent intent = new Intent(View_Offers.this, FarmerLocation.class);
                                intent.putExtra("latitude", lat);
                                intent.putExtra("longitude", lng);
                                intent.putExtra("farmerName", farmerName);
                                intent.putExtra("farmerAddress", farmerAddress);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(View_Offers.this, "Location not available.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(View_Offers.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }

            return view;
        }
    }
}
