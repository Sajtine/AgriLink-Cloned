package com.example.loginapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.database.Cursor;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class Product_History extends AppCompatActivity {

    ListView productHistoryListView;
    ArrayList<HashMap<String, String>> historyList;
    MyDatabaseHelper dbHelper;
    TextView noHistoryMessage;
    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_history);

        productHistoryListView = findViewById(R.id.product_historyListView);
        dbHelper = new MyDatabaseHelper(this);
        historyList = new ArrayList<>();
        noHistoryMessage = findViewById(R.id.noHistoryMessage);


        back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());

        loadProductHistory();

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                historyList,
                R.layout.product_history_card,
                new String[]{"farmer", "product", "price", "quantity", "status", "received_date"},
                new int[]{R.id.farmerName, R.id.productName, R.id.productPrice, R.id.productQuantity, R.id.status, R.id.receivedDate}
        );
        productHistoryListView.setAdapter(adapter);

    }

    private void loadProductHistory(){
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int vendorId = Integer.parseInt(preferences.getString("userId", "0"));

        Cursor cursor = dbHelper.getAllReceivedProducts(vendorId);
        historyList.clear();

        if(cursor != null && cursor.moveToFirst()){

            do{
                HashMap<String, String> map = new HashMap<>();
                map.put("farmer", "👨‍🌾 " + cursor.getString(cursor.getColumnIndexOrThrow("farmer_name")));
                map.put("product", "Product: " + cursor.getString(cursor.getColumnIndexOrThrow("product_name")));
                map.put("price", "Price: ₱" + cursor.getString(cursor.getColumnIndexOrThrow("price")) + " / kilos");
                map.put("quantity", "Quantity:  " + cursor.getString(cursor.getColumnIndexOrThrow("quantity")) + " kilos");
                map.put("status", "Status: " + cursor.getString(cursor.getColumnIndexOrThrow("status")));
                map.put("received_date", "Received: " + cursor.getString(cursor.getColumnIndexOrThrow("received_date")));
                historyList.add(map);
            }while(cursor.moveToNext());

        }else{
            noHistoryMessage.setVisibility(View.VISIBLE);

        }

    }
}