package com.example.loginappclone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatUserAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> chatUserUIDs;

    // Static maps for last messages, times, unread counts
    public static HashMap<String, String> lastMessages = new HashMap<>();
    public static HashMap<String, String> lastMessageTimes = new HashMap<>();
    public static HashMap<String, Integer> unreadCounts = new HashMap<>();

    // Map to store fetched usernamessZ
    public static HashMap<String, String> uidToUsername = new HashMap<>();

    public ChatUserAdapter(Context context, ArrayList<String> chatUserUIDs) {
        this.context = context;
        this.chatUserUIDs = chatUserUIDs;
    }

    @Override
    public int getCount() {
        return chatUserUIDs.size();
    }

    @Override
    public Object getItem(int position) {
        return chatUserUIDs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chatlist_item, parent, false);
        }

        String uid = chatUserUIDs.get(position);
        TextView tvUsername = convertView.findViewById(R.id.chat_username);
        TextView tvLastMessage = convertView.findViewById(R.id.chat_last_message);
        TextView tvTime = convertView.findViewById(R.id.chat_time);
        TextView tvUnread = convertView.findViewById(R.id.chat_unread_count);
        ImageView profile = convertView.findViewById(R.id.chat_profile);

        // Set last message, time, unread count
        tvLastMessage.setText(lastMessages.getOrDefault(uid, "Typing..."));
        tvTime.setText(lastMessageTimes.getOrDefault(uid, ""));
        int unread = unreadCounts.getOrDefault(uid, 0);
        tvUnread.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
        tvUnread.setText(String.valueOf(unread));

        // Fetch username if not already fetched
        if (uidToUsername.containsKey(uid)) {
            tvUsername.setText(uidToUsername.get(uid));
        } else {
            tvUsername.setText("Loading...");
            fetchUsername(uid, tvUsername);
        }

        return convertView;
    }

    private void fetchUsername(@NonNull String uid, @NonNull TextView tvUsername) {
        DatabaseReference farmersRef = FirebaseDatabase.getInstance()
                .getReference("users/farmers/" + uid + "/username");
        DatabaseReference vendorsRef = FirebaseDatabase.getInstance()
                .getReference("users/vendors/" + uid + "/username");

        farmersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);
                if (username != null) {
                    uidToUsername.put(uid, username);
                    tvUsername.setText(username);
                } else {
                    vendorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot2) {
                            String username2 = snapshot2.getValue(String.class);
                            uidToUsername.put(uid, username2 != null ? username2 : "Unknown");
                            tvUsername.setText(uidToUsername.get(uid));
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
}
