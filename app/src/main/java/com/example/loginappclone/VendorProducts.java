package com.example.loginappclone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendorProducts extends AppCompatActivity {

    ListView listViewProducts;
    ArrayAdapter<String> adapter;
    ArrayList<String> productList;

    DatabaseReference vendorProductsRef;
    String vendorUID;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor_products);

        listViewProducts = findViewById(R.id.listViewProducts);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("uid", null);

        if (currentUser == null) {
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        vendorUID = currentUser;
        vendorProductsRef = FirebaseDatabase.getInstance().getReference("vendor_products").child(vendorUID);

        productList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, productList);
        listViewProducts.setAdapter(adapter);

        loadProducts();

        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = productList.get(position);
            showEditDeleteDialog(selectedItem);
        });

        // Back button
        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());
    }

    private void loadProducts() {
        vendorProductsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();

                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    String name = productSnap.child("vendor_product_name").getValue(String.class);
                    Integer price = productSnap.child("vendor_product_price").getValue(Integer.class);
                    String unit = productSnap.child("product_unit").getValue(String.class);

                    if (name != null && price != null && unit != null) {
                        productList.add(name + " - ₱" + price + "/" + unit);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorProducts.this, "Error loading products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDeleteDialog(String selectedItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showUpdateDialog(selectedItem);
                    } else {
                        deleteProduct(selectedItem);
                    }
                });
        builder.show();
    }

    private void deleteProduct(String selectedItem) {
        String productName = selectedItem.split(" - ₱")[0];

        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete: " + productName + "?")
                .setPositiveButton("Yes", (confirmDialog, i) -> {
                    vendorProductsRef.orderByChild("vendor_product_name").equalTo(productName)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot productSnap : snapshot.getChildren()) {
                                        productSnap.getRef().removeValue();
                                    }
                                    Toast.makeText(VendorProducts.this, "Product deleted!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(VendorProducts.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showUpdateDialog(String selectedItem) {
        // Split format: Name - ₱Price/Unit
        String[] parts = selectedItem.split(" - ₱|/");
        String currentName = parts[0];
        String currentPrice = parts[1];
        String currentUnit = parts[2];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.update_products, null);
        builder.setView(view);

        EditText nameEditText = view.findViewById(R.id.productName);
        EditText priceEditText = view.findViewById(R.id.productPrice);
        Spinner unitSpinner = view.findViewById(R.id.spinnerUnit);
        Button saveButton = view.findViewById(R.id.btnSaveProduct);

        nameEditText.setText(currentName);
        priceEditText.setText(currentPrice);

        List<String> units = new ArrayList<>();
        units.add("kg");
        units.add("bunches");
        units.add("pieces");

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, units);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item);
        unitSpinner.setAdapter(unitAdapter);

        int unitPosition = unitAdapter.getPosition(currentUnit.trim());
        unitSpinner.setSelection(unitPosition);

        AlertDialog dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newPriceText = priceEditText.getText().toString().trim();
            String newUnit = unitSpinner.getSelectedItem().toString();

            if (newName.isEmpty() || newPriceText.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            int newPrice;
            try {
                newPrice = Integer.parseInt(newPriceText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }

            vendorProductsRef.orderByChild("vendor_product_name").equalTo(currentName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot productSnap : snapshot.getChildren()) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("vendor_product_name", newName);
                                updates.put("vendor_product_price", newPrice);
                                updates.put("product_unit", newUnit);

                                productSnap.getRef().updateChildren(updates);
                            }
                            Toast.makeText(VendorProducts.this, "Product updated!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(VendorProducts.this, "Failed to update", Toast.LENGTH_SHORT).show();
                        }
                    });

            dialog.dismiss();
        });
    }
}
