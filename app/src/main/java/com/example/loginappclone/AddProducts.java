package com.example.loginappclone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AddProducts extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    DatabaseReference vendorProductsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Firebase reference for vendor_products
        String currentUser = sharedPreferences.getString("uid", null);
        if (currentUser != null) {
            String vendorUID = currentUser;
            vendorProductsRef = FirebaseDatabase.getInstance().getReference("vendor_products").child(vendorUID);
        } else {
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

            String product_name = productName.getText().toString().trim();
            String priceText = productPrice.getText().toString().trim();
            String unit = unitSpinner.getSelectedItem().toString();

            // Check if all fields are filled
            if (product_name.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert price to integer
            int price;
            try {
                price = Integer.parseInt(priceText);
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a unique product ID in Firebase
            String productId = vendorProductsRef.push().getKey();

            // Create product map
            Map<String, Object> productData = new HashMap<>();
            productData.put("vendor_product_id", productId);
            productData.put("vendor_product_name", product_name);
            productData.put("vendor_product_price", price); // stored as Integer
            productData.put("product_unit", unit);

            // Save to Firebase
            if (productId != null) {
                vendorProductsRef.child(productId).setValue(productData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                                productName.setText("");
                                productPrice.setText("");
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to add product", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
