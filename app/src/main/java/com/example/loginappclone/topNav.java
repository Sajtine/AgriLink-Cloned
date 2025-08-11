package com.example.loginappclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class topNav extends Fragment {

    private ImageView messages;
    private TextView badgeCount;

    public topNav() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_nav, container, false);

        messages = view.findViewById(R.id.messages);
        badgeCount = view.findViewById(R.id.badgeCount); // ← make sure badgeCount exists in your layout

        messages.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), ChatList.class));
        });

        checkUnreadMessages(); // Fetch unread messages count from Firebase

        return view;
    }


    public void refreshBadge() {
        checkUnreadMessages(); // manually call it when Home resumes
    }

    private void checkUnreadMessages() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUser = prefs.getString("username", "");

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;

                for (DataSnapshot roomSnap : snapshot.getChildren()) {
                    for (DataSnapshot msgSnap : roomSnap.getChildren()) {
                        Message msg = msgSnap.getValue(Message.class);
                        if (msg != null && msg.receiverId.equals(currentUser) && !msg.isRead) {
                            unreadCount++;
                        }
                    }
                }

                if (unreadCount > 0) {
                    showBadge(unreadCount);
                } else {
                    hideBadge();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showBadge(int count) {
        badgeCount.setText(String.valueOf(count));
        badgeCount.setVisibility(View.VISIBLE);
    }

    private void hideBadge() {
        badgeCount.setVisibility(View.GONE);
    }
}
