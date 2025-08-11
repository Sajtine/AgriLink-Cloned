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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class VendorProducts extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    MyDatabaseHelper databaseHelper;
    
    ListView listViewProducts;
    ArrayAdapter<String> adapter;
    ArrayList<String> productList;
    int vendorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor_products);

        listViewProducts = findViewById(R.id.listViewProducts);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        databaseHelper = new MyDatabaseHelper(this);
        vendorId = Integer.parseInt(sharedPreferences.getString("userId", null));

        productList = databaseHelper.getVendorProducts(vendorId);

        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, productList);
        listViewProducts.setAdapter(adapter);

        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = productList.get(position);
            // You can use dialog here to update or delete
            showEditDeleteDialog(selectedItem);
        });

        // Button Back
        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> {
            finish();
        });

    }

    private void showEditDeleteDialog(String selectedItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showUpdateDialog(selectedItem);
                    } else {

                        String productName = selectedItem.split(" - ₱")[0];

                        new AlertDialog.Builder(this)
                                .setTitle("Delete Product")
                                .setMessage("Are you sure you want to delete the product: " + productName + "?")
                                .setPositiveButton("Yes", (confirmDialog, i) -> {
                                    boolean isDeleted = databaseHelper.deleteVendorProduct(vendorId, selectedItem.split(" - ₱")[0]);
                                    if (isDeleted) {
                                        productList.clear();
                                        productList.addAll(databaseHelper.getVendorProducts(vendorId));
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(this, "Product deleted!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to delete product.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
        builder.show();
    }



    private void showUpdateDialog(String selectedItem) {
        // Split selected product info assuming format: Name - ₱Price/Unit
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

        // Set current values
        nameEditText.setText(currentName);
        priceEditText.setText(currentPrice);

        // Basic unit options added directly here
        List<String> units = new ArrayList<>();
        units.add("kg");
        units.add("bunches");
        units.add("pieces");

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, units);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item);
        unitSpinner.setAdapter(unitAdapter);

        // Set the selected unit properly
        int unitPosition = unitAdapter.getPosition(currentUnit.trim());
        unitSpinner.setSelection(unitPosition);

        AlertDialog dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newPrice = priceEditText.getText().toString().trim();
            String newUnit = unitSpinner.getSelectedItem().toString();

            // Update in database
            boolean isUpdated = databaseHelper.updateVendorProduct(vendorId, currentName, newName, newPrice, newUnit);

            if(isUpdated){
                Toast.makeText(VendorProducts.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();

                // Refresh list
                productList.clear();
                productList.addAll(databaseHelper.getVendorProducts(vendorId));
                adapter.notifyDataSetChanged();
            }else{
                Toast.makeText(VendorProducts.this, "Failed to update product.", Toast.LENGTH_SHORT).show();
            }


            dialog.dismiss();
        });
    }


}