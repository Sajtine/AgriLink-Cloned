package com.example.loginappclone;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class Request_Sell extends DialogFragment {

    private String vendorUID, vendorName, marketName;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    private static final int PICK_LOCATION_REQUEST_CODE = 1001;

    // Selected location
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request_sell, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        if (getArguments() != null) {
            vendorUID = getArguments().getString("vendor_id");
            vendorName = getArguments().getString("vendorName");
            marketName = getArguments().getString("marketName");
        }

        EditText productName = view.findViewById(R.id.input_product_name);
        EditText quantity = view.findViewById(R.id.input_quantity);
        EditText price = view.findViewById(R.id.input_price);
        EditText deliveryDate = view.findViewById(R.id.input_delivery_date);
        Button submitBtn = view.findViewById(R.id.submit_request);
        Spinner spinner = view.findViewById(R.id.payment_method);
        RadioGroup radioGroup = view.findViewById(R.id.pickup_option_group);
        Button btnPickLocation = view.findViewById(R.id.btn_pick_location);

        // Date picker
        deliveryDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        deliveryDate.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH).format(selectedDate.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Payment spinner
        List<String> paymentOptions = Collections.singletonList("Cash on Delivery");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, paymentOptions);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        // Show/hide pick location button based on pickup option
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.option_vendor_pickup) {
                btnPickLocation.setVisibility(View.VISIBLE);
            } else {
                btnPickLocation.setVisibility(View.GONE);
                selectedLatitude = null;
                selectedLongitude = null;
            }
        });

        // Open MapPicker when button clicked
        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MapPicker.class);
            startActivityForResult(intent, PICK_LOCATION_REQUEST_CODE);
        });

        submitBtn.setOnClickListener(v -> {
            String name = productName.getText().toString().trim();
            String qtyStr = quantity.getText().toString().trim();
            String priceStr = price.getText().toString().trim();
            String date = deliveryDate.getText().toString().trim();
            String paymentMethod = spinner.getSelectedItem().toString().trim();

            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "Please select a pickup option", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selectedRadio = view.findViewById(selectedId);
            String pickUpOption = selectedRadio.getText().toString();

            if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Require location if vendor will pick up
            if (pickUpOption.equalsIgnoreCase("Vendor will pick up the product") &&
                    (selectedLatitude == null || selectedLongitude == null)) {
                Toast.makeText(getContext(), "Please select pick-up location", Toast.LENGTH_SHORT).show();
                return;
            }

            submitRequest(name, qtyStr, priceStr, date, paymentMethod, pickUpOption, selectedLatitude, selectedLongitude);


        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_LOCATION_REQUEST_CODE && data != null) {
            selectedLatitude = data.getDoubleExtra("latitude", 0);
            selectedLongitude = data.getDoubleExtra("longitude", 0);
            Toast.makeText(getContext(), "Location selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitRequest(String name, String qtyStr, String priceStr, String date, String paymentMethod,
                               String pickUpOption, Double latitude, Double longitude) {


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUserUID = sharedPreferences.getString("uid", null);

        assert currentUserUID != null;
        dbRef.child("users").child("farmers").child(currentUserUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String farmerName = snapshot.child("username").getValue(String.class);
                        String phoneNumber = snapshot.child("phone_number").getValue(String.class);
                        String address = snapshot.child("address").getValue(String.class);

                        if (phoneNumber == null || phoneNumber.isEmpty() || address == null || address.isEmpty()) {
                            Toast.makeText(getContext(), "Please complete your profile info first", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Request_Sell.this.getContext(), Farmers_Details.class);
                            startActivity(intent);
                            requireActivity().finish();
                            return;
                        }

                        int qty = Integer.parseInt(qtyStr);
                        double prc = Double.parseDouble(priceStr);
                        String timestamp = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(new Date());

                        DatabaseReference requestRef = dbRef.child("requests").child(vendorUID).push();
                        requestRef.child("productName").setValue(name);
                        requestRef.child("quantity").setValue(qty);
                        requestRef.child("price").setValue(prc);
                        requestRef.child("deliveryDate").setValue(date);
                        requestRef.child("vendorUID").setValue(vendorUID);
//                        requestRef.child("vendorName").setValue(vendorName);      // Data Normalization
//                        requestRef.child("marketName").setValue(marketName);      // Data Normalization
                        requestRef.child("farmerUID").setValue(currentUserUID);
//                        requestRef.child("farmerName").setValue(farmerName);      // Data Normalization
//                        requestRef.child("phoneNumber").setValue(phoneNumber);    // Data Normalization
//                        requestRef.child("address").setValue(address);            // Data Normalization
                        requestRef.child("requestDate").setValue(timestamp);
                        requestRef.child("pickupOption").setValue(pickUpOption);
                        requestRef.child("paymentMethod").setValue(paymentMethod);
                        requestRef.child("status").setValue("Pending");

                        // Save location only if vendor will pick up
                        if (latitude != null && longitude != null) {
                            requestRef.child("latitude").setValue(latitude);
                            requestRef.child("longitude").setValue(longitude);
                        }

                        Toast.makeText(getContext(), "Request Sent!", Toast.LENGTH_SHORT).show();

                        // Send notification to Vendor
                        // Inside Request_Sell fragment
                        Context appContext = getActivity() != null ? getActivity().getApplicationContext() : null;
                        sendNotificationToVendorPusher(appContext, vendorUID, "New Sell Offer",
                                "Farmer " + farmerName + " sent an offer.");


                        dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getTheme() {
        return R.style.RequestSellDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // Pusher Notification (Beam)
    public static void sendNotificationToVendorPusher(Context appContext, String vendorUID, String title, String message) {
        if (appContext == null) {
            Log.e("PUSHER", "Application context is null! Cannot send notification.");
            return;
        }

        try {

            JSONObject json = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject fcm = new JSONObject();
            JSONObject notification = new JSONObject();

            notification.put("title", title);
            notification.put("body", message);

            // Foreground Notification
            data.put("title", title);
            data.put("body", message);

            // Background Notification
            fcm.put("notification", notification);
            fcm.put("data", data);

            JSONArray interests = new JSONArray();
            interests.put("vendor_" + vendorUID);

            json.put("interests", interests);
            json.put("fcm", fcm);

            String url = "https://82e5c130-03f5-40a0-9494-22f7bc17bc27.pushnotifications.pusher.com/publish_api/v1/instances/82e5c130-03f5-40a0-9494-22f7bc17bc27/publishes";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> Log.d("PUSHER", "Notification sent successfully: " + response.toString()),
                    error -> Log.e("PUSHER", "Error sending notification: " + error.toString())
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer ACC2F781E0170E544CB222730E491FEFA38C499FD863DD756EF58699578E27CF");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            Volley.newRequestQueue(appContext).add(request);

        } catch (Exception e) {
            Log.e("PUSHER", "Exception while sending notification: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
