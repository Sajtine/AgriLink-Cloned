package com.example.loginappclone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    ListView chatList;
    EditText messageInput;
    ImageButton send;
    ArrayList<Message> messages;
    ArrayAdapter<Message> adapter;
    DatabaseReference dbRef;
    String currentUser, chatWith, chatRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);

        chatList = findViewById(R.id.chatList);
        messageInput = findViewById(R.id.messageInput);
        send = findViewById(R.id.sendBtn);

        // Get current user from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUser = prefs.getString("uid", "Anonymous");

        // Get person to chat with
        chatWith = getIntent().getStringExtra("chatWithUID");

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(chatWith)
                .child("username");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(username != null ? username : "Chat");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Generate chatRoom name
        chatRoom = currentUser.compareTo(chatWith) < 0
                ? currentUser + "_" + chatWith
                : chatWith + "_" + currentUser;

        dbRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoom);

        messages = new ArrayList<>();

        // Custom adapter
        adapter = new ArrayAdapter<Message>(this, R.layout.chat_item, R.id.chat_message, messages) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView msgText = view.findViewById(R.id.chat_message);
                RelativeLayout layout = (RelativeLayout) view;

                Message message = messages.get(position);
                msgText.setText(message.message);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msgText.getLayoutParams();

                // Clear alignment
                params.removeRule(RelativeLayout.ALIGN_PARENT_START);
                params.removeRule(RelativeLayout.ALIGN_PARENT_END);

                if (message.senderId.equals(currentUser)) {
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                    msgText.setBackgroundResource(R.drawable.message_bg_self); // your drawable
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_START);
                    msgText.setBackgroundResource(R.drawable.message_bg); // your drawable
                }

                msgText.setLayoutParams(params);
                return view;
            }
        };

        chatList.setAdapter(adapter);

        send.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();

            if (!text.isEmpty()) {
                Message msg = new Message(currentUser, chatWith, text, System.currentTimeMillis(), false);
                dbRef.push().setValue(msg);
                messageInput.setText("");
            }
        });

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message msg = snap.getValue(Message.class);
                    if (msg != null) {
                        messages.add(msg);

                        if (msg.receiverId.equals(currentUser)
                                && msg.senderId.equals(chatWith)
                                && !msg.isRead) {
                            snap.getRef().child("isRead").setValue(true);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                chatList.setSelection(adapter.getCount() - 1); // scroll to bottom
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error
            }
        });

    }

    // method for back arrow
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
