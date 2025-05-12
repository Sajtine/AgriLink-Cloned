package com.example.loginapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class Product_History extends AppCompatActivity {

    ListView product_history;
    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_history);

        product_history = findViewById(R.id.product_historyListView);

        back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());
    }
}