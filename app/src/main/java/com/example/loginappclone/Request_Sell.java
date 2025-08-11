package com.example.loginappclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Request_Sell extends DialogFragment {

    private static final String ARG_VENDOR_ID = "vendor_id";
    private int vendorId;
    private MyDatabaseHelper db;

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

        View view = inflater.inflate(R.layout.fragment_request_sell, container, false);

        // Optional: Remove the top padding from the dialog background
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        String userId = sharedPreferences.getString("userId", null);
        String username = sharedPreferences.getString("username", null);

        db = new MyDatabaseHelper(getContext());

        EditText productName = view.findViewById(R.id.input_product_name);
        EditText quantity = view.findViewById(R.id.input_quantity);
        EditText price = view.findViewById(R.id.input_price);
        EditText deliveryDate = view.findViewById(R.id.input_delivery_date);
        Button submitBtn = view.findViewById(R.id.submit_request);

        if (getArguments() != null) {
            vendorId = Integer.parseInt(getArguments().getString(ARG_VENDOR_ID));
        }

        // Handle delivery date picker
        deliveryDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new android.app.DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        String formattedDate = new SimpleDateFormat("MMMM dd, yyyy").format(selectedDate.getTime());
                        deliveryDate.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Set up payment option spinner
        Spinner spinner = view.findViewById(R.id.payment_method);

        // Check if Spinner was found
        if (spinner != null) {
            List<String> paymentOptions = new ArrayList<>();
            paymentOptions.add("Cash on Delivery");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    R.layout.spinner_item,
                    paymentOptions
            );
            adapter.setDropDownViewResource(R.layout.spinner_item);

            spinner.setAdapter(adapter);
        } else {
            Log.e("RequestSell", "Spinner not found!");
            Toast.makeText(getContext(), "Payment spinner not found in layout!", Toast.LENGTH_SHORT).show();
        }


        submitBtn.setOnClickListener(v -> {
            String name = productName.getText().toString().trim();
            String qtyStr = quantity.getText().toString().trim();
            String priceStr = price.getText().toString().trim();
            String date = deliveryDate.getText().toString().trim();
            String paymentMethod = spinner.getSelectedItem().toString();

            // Get selected option in radio button
            RadioGroup radioGroup = view.findViewById(R.id.pickup_option_group);

            int selectedId = radioGroup.getCheckedRadioButtonId();
            String pickUpOption = "";

            if (selectedId != -1){

                RadioButton selectedRadio = view.findViewById(selectedId);

                pickUpOption = selectedRadio.getText().toString();

            }

            if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = db.getUserDetails(email);
            if (cursor != null && cursor.moveToFirst()) {
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                if (phoneNumber == null || phoneNumber.isEmpty() || address == null || address.isEmpty()) {
                    Toast.makeText(getContext(), "Please complete your profile info first", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), Farmers_Details.class);
                    intent.putExtra("redirectToMarket", true);
                    startActivity(intent);
                    dismiss();
                    return;
                }

                int qty = Integer.parseInt(qtyStr);
                double prc = Double.parseDouble(priceStr);
                String timestamp = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(new Date());

                boolean inserted = db.insertRequest(name, qty, prc, date, vendorId,
                        Integer.parseInt(userId), username, phoneNumber, timestamp, pickUpOption, paymentMethod);

                if (inserted) {
                    Toast.makeText(getContext(), "Request Sent!", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to send request.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
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
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }


}
