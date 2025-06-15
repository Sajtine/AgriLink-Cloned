package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatList extends AppCompatActivity {

    ListView chatListView;
    ArrayList<String> chatUsers;
    ChatUserAdapter adapter;
    DatabaseReference dbRef;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatListView = findViewById(R.id.chatListView);

        // Get current user
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUser = prefs.getString("username", "Anonymous");

        // Initialize adapter
        chatUsers = new ArrayList<>();
        adapter = new ChatUserAdapter(this, chatUsers);
        chatListView.setAdapter(adapter);

        // Reference to Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("chats");

        // Load chats
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                chatUsers.clear();
                ChatUserAdapter.lastMessages.clear();
                ChatUserAdapter.lastMessageTimes.clear();

                for (DataSnapshot chatRoom : snapshot.getChildren()) {
                    String key = chatRoom.getKey();

                    if (key.contains(currentUser)) {
                        String[] users = key.split("_");
                        String otherUser = users[0].equals(currentUser) ? users[1] : users[0];

                        if (!chatUsers.contains(otherUser)) {
                            chatUsers.add(otherUser);
                        }

                        // ✅ Get latest message by timestamp
                        Message latestMsg = null;
                        for (DataSnapshot msgSnap : chatRoom.getChildren()) {
                            Message msg = msgSnap.getValue(Message.class);

                            if (msg != null &&
                                    msg.senderId != null &&
                                    msg.receiverId != null &&
                                    (msg.senderId.equals(currentUser) || msg.receiverId.equals(currentUser))) {

                                if (latestMsg == null || msg.timestamp > latestMsg.timestamp) {
                                    latestMsg = msg;
                                }
                            }
                        }

                        // ✅ Store only the latest message
                        if (latestMsg != null) {
                            ChatUserAdapter.lastMessages.put(otherUser, latestMsg.message);
                            ChatUserAdapter.lastMessageTimes.put(otherUser, formatTime(latestMsg.timestamp));
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Optional: handle error
            }
        });

        // Open chat on item click
        chatListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUser = chatUsers.get(position);
            Intent intent = new Intent(ChatList.this, Chat.class);
            intent.putExtra("chatWith", selectedUser);
            startActivity(intent);
        });
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        return sdf.format(new Date(timestamp));
    }
}
