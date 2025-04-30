package com.example.loginapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;


public class Request_Sell extends DialogFragment {

    private static final String ARG_VENDOR_ID = "vendor_id";
    private int vendorId;

    public static Request_Sell newInstance(String vendorId) {
        Request_Sell fragment = new Request_Sell();
        Bundle args = new Bundle();
        args.putString(ARG_VENDOR_ID, vendorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_request_sell, container, false);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        MyDatabaseHelper db = new MyDatabaseHelper(getContext());
        Cursor cursor = db.getUserDetails(email);

        String phone_number;

        if(cursor.moveToFirst()){
            phone_number = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
        } else {
            phone_number = "";
        }


        EditText productName = view.findViewById(R.id.input_product_name);
        EditText quantity = view.findViewById(R.id.input_quantity);
        EditText price = view.findViewById(R.id.input_price);
        EditText notes = view.findViewById(R.id.input_notes);
        Button submitBtn = view.findViewById(R.id.submit_request);

        if (getArguments() != null) {
            vendorId = Integer.parseInt(getArguments().getString(ARG_VENDOR_ID));
        }


        EditText deliveryDate = view.findViewById(R.id.input_delivery_date);

        deliveryDate.setOnClickListener(v -> {
            // Get current date
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            // Show the DatePickerDialog
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    Request_Sell.this.getContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        // Format date as April 13, 2025
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy");
                        java.util.Calendar selectedCal = java.util.Calendar.getInstance();
                        selectedCal.set(selectedYear, selectedMonth, selectedDay);
                        String formattedDate = sdf.format(selectedCal.getTime());

                        deliveryDate.setText(formattedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });


        submitBtn.setOnClickListener(v -> {
            String name = productName.getText().toString().trim();
            String qty = quantity.getText().toString().trim();
            String prc = price.getText().toString().trim();
            String note = notes.getText().toString().trim();
            String date = deliveryDate.getText().toString().trim();

            String userId = sharedPreferences.getString("userId", null);
            String username = sharedPreferences.getString("username", null);
            String farmer_number = phone_number;

            if (name.isEmpty() || qty.isEmpty() || prc.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            } else {
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(getContext());
                boolean inserted = dbHelper.insertRequest(name, Integer.parseInt(qty), Double.parseDouble(prc), date, note, vendorId, Integer.parseInt(userId), username, farmer_number);

                if (inserted) {
                    Toast.makeText(getContext(), "Request Sent!", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to send request.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    @Override
    public int getTheme() {
        return android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;

            // DON'T use transparent, retain background
            getDialog().getWindow().setLayout(width, height);
        }
    }

}
