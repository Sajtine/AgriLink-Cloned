package com.example.loginappclone;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class View_Offers extends AppCompatActivity {

    ListView offersListView;
    Button backButton;
    ArrayList<String> offerList;
    ArrayList<Integer> offerIdList; // store IDs here
    MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_offers);

        offersListView = findViewById(R.id.offersListView);
        backButton = findViewById(R.id.backButton);
        dbHelper = new MyDatabaseHelper(this);

        loadFarmerOffers();

        backButton.setOnClickListener(v -> finish());
    }

    private void loadFarmerOffers() {
        offerList = new ArrayList<>();
        offerIdList = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int vendorId = Integer.parseInt(sharedPreferences.getString("userId", "0"));

        Cursor cursor = dbHelper.getAllFarmerOffers(vendorId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int offerId = cursor.getInt(cursor.getColumnIndexOrThrow("id")); // assuming 'id' is COLUMN_PRODUCT_OFFER_ID
                String farmerName = cursor.getString(cursor.getColumnIndexOrThrow("farmer_name"));
                String farmer_number = cursor.getString(cursor.getColumnIndexOrThrow("farmer_number"));
                String product = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                String price = cursor.getString(cursor.getColumnIndexOrThrow("price"));
                String quantity = cursor.getString(cursor.getColumnIndexOrThrow("quantity"));
                String deliveryDate = cursor.getString(cursor.getColumnIndexOrThrow("delivery_date"));

                String offerDetails = "👨‍🌾 " + farmerName +
                        "\nProduct: " + product +
                        "\nFarmer No. " + farmer_number +
                        "\nPrice: ₱" + price + " / kilo" +
                        "\nQuantity: " + quantity + " kilos" +
                        "\nDelivery Date: " + deliveryDate;

                offerList.add(offerDetails);
                offerIdList.add(offerId);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            offerList.add("No offers available.");
        }

        OfferAdapter adapter = new OfferAdapter();
        offersListView.setAdapter(adapter);
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

            txtOfferDetails.setText(offerList.get(position));

            // Check Offers
            if (offerList.get(position).equals("No offers available.")) {
                btnAccept.setVisibility(View.GONE);
                btnDecline.setVisibility(View.GONE);
            } else {
                btnAccept.setVisibility(View.VISIBLE);
                btnDecline.setVisibility(View.VISIBLE);

                btnAccept.setOnClickListener(v -> {
                    int offerId = offerIdList.get(position);
                    dbHelper.updateOfferStatus(offerId, "Accepted");
                    Toast.makeText(View_Offers.this, "Offer accepted!", Toast.LENGTH_SHORT).show();

                    // remove item from list and refresh
                    offerList.remove(position);
                    offerIdList.remove(position);
                    notifyDataSetChanged();
                });

                btnDecline.setOnClickListener(v -> {
                    int offerId = offerIdList.get(position);
                    dbHelper.updateOfferStatus(offerId, "Declined");
                    Toast.makeText(View_Offers.this, "Offer declined.", Toast.LENGTH_SHORT).show();

                    // remove item from list and refresh
                    offerList.remove(position);
                    offerIdList.remove(position);
                    notifyDataSetChanged();
                });
            }

            return view;
        }
    }
}
