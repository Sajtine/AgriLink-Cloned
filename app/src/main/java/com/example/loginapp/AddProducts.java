package com.example.loginapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class AddProducts extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    MyDatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        myDB = new MyDatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Product Details
        EditText productName = findViewById(R.id.productName);
        EditText productPrice = findViewById(R.id.productPrice);

        Spinner unitSpinner = findViewById(R.id.spinnerUnit);

        List<String> units = new ArrayList<>();
        units.add("kg");
        units.add("bunches");
        units.add("pieces");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, units);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        unitSpinner.setAdapter(adapter);

        // save button
        Button saveProducts = findViewById(R.id.btnSaveProduct);
        saveProducts.setOnClickListener(view -> {

            String product_name = productName.getText().toString();
            String priceText = productPrice.getText().toString();
            String unit = unitSpinner.getSelectedItem().toString();
            String userId = sharedPreferences.getString("userId", null);
            int vendor_id = Integer.parseInt(userId);

            // Check if all fields are filled
            if (product_name.isEmpty() || priceText.isEmpty() || userId == null) {
                Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return; // Stop further execution
            }

            // Convert price to double
            double price = Double.parseDouble(priceText);

            // Add the product
            boolean isInserted = myDB.addVendorProducts(product_name, price, unit, vendor_id);

            if (isInserted) {
                Toast.makeText(getApplicationContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                productName.setText("");
                productPrice.setText("");
            } else {
                Toast.makeText(getApplicationContext(), "Product already exist!", Toast.LENGTH_SHORT).show();
            }
        });


    }
}