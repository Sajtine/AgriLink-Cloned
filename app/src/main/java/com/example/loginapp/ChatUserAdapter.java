package com.example.loginapp;

import android.content.Context;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatUserAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> users;
    LayoutInflater inflater;

    // ✅ Added for last message and time
    public static HashMap<String, String> lastMessages = new HashMap<>();
    public static HashMap<String, String> lastMessageTimes = new HashMap<>();

    public ChatUserAdapter(Context context, ArrayList<String> users) {
        this.context = context;
        this.users = users;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView profile;
        TextView username, lastMessage, time;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.chatlist_item, parent, false);
            holder = new ViewHolder();
            holder.profile = convertView.findViewById(R.id.chat_profile);
            holder.username = convertView.findViewById(R.id.chat_username);
            holder.lastMessage = convertView.findViewById(R.id.chat_last_message);
            holder.time = convertView.findViewById(R.id.chat_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set values
        String user = users.get(position);
        holder.username.setText(user);

        // ✅ Display the last message and time (if available)
        if (lastMessages.containsKey(user)) {
            holder.lastMessage.setText(lastMessages.get(user));
        } else {
            holder.lastMessage.setText("No messages yet");
        }

        if (lastMessageTimes.containsKey(user)) {
            holder.time.setText(lastMessageTimes.get(user));
        } else {
            holder.time.setText("");
        }

        // Set default profile image
        holder.profile.setImageResource(R.drawable.person);

        return convertView;
    }
}
