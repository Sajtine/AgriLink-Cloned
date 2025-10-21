package com.example.loginappclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

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
    TextView noChatsMessage;

    ArrayList<String> chatUsersUIDs = new ArrayList<>();
    ChatUserAdapter adapter;
    String currentUserUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatListView = findViewById(R.id.chatListView);
        noChatsMessage = findViewById(R.id.noChatsMessage);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserUID = prefs.getString("uid", "");

        adapter = new ChatUserAdapter(this, chatUsersUIDs);
        chatListView.setAdapter(adapter);

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                chatUsersUIDs.clear();
                ChatUserAdapter.lastMessages.clear();
                ChatUserAdapter.lastMessageTimes.clear();
                ChatUserAdapter.unreadCounts.clear();

                for (DataSnapshot chatRoom : snapshot.getChildren()) {
                    String otherUID = null;
                    String latestMsg = "";
                    long latestTime = 0;
                    int unread = 0;

                    for (DataSnapshot msgSnap : chatRoom.getChildren()) {
                        Message msg = msgSnap.getValue(Message.class);
                        if (msg == null) continue;

                        if (msg.senderId.equals(currentUserUID)) {
                            otherUID = msg.receiverId;
                        } else if (msg.receiverId.equals(currentUserUID)) {
                            otherUID = msg.senderId;
                        }

                        if (otherUID == null) continue;

                        if (msg.timestamp > latestTime) {
                            latestMsg = msg.message;
                            latestTime = msg.timestamp;
                        }

                        if (msg.receiverId.equals(currentUserUID) && !msg.isRead) {
                            unread++;
                        }
                    }

                    if (otherUID != null && !chatUsersUIDs.contains(otherUID)) {
                        chatUsersUIDs.add(otherUID);

                        // Fetch username for display
                        fetchUsername(otherUID);
                    }

                    if (otherUID != null) {
                        ChatUserAdapter.lastMessages.put(otherUID, latestMsg);
                        ChatUserAdapter.lastMessageTimes.put(otherUID, formatTime(latestTime));
                        ChatUserAdapter.unreadCounts.put(otherUID, unread);
                    }
                }

                adapter.notifyDataSetChanged();

                if (chatUsersUIDs.isEmpty()) {
                    noChatsMessage.setVisibility(TextView.VISIBLE);
                    chatListView.setVisibility(ListView.GONE);
                } else {
                    noChatsMessage.setVisibility(TextView.GONE);
                    chatListView.setVisibility(ListView.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        chatListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUID = chatUsersUIDs.get(position);
            Intent intent = new Intent(ChatList.this, Chat.class);
            intent.putExtra("chatWithUID", selectedUID);
            startActivity(intent);
        });
    }

    private void fetchUsername(String uid) {
        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
                .getReference("users/farmers/" + uid + "/username");
        DatabaseReference vendorsRef = FirebaseDatabase.getInstance()
                .getReference("users/vendors/" + uid + "/username");

        farmersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                String username = snap.getValue(String.class);
                if (username != null) {
                    ChatUserAdapter.uidToUsername.put(uid, username);
                    adapter.notifyDataSetChanged();
                } else {
                    vendorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snap2) {
                            String username2 = snap2.getValue(String.class);
                            ChatUserAdapter.uidToUsername.put(uid, username2 != null ? username2 : "Unknown");
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        return sdf.format(new Date(timestamp));
    }
}
