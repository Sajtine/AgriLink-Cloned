package com.example.loginappclone;

public class Message {
    public String senderId;
    public String receiverId;
    public String message;
    public long timestamp;
    public boolean isRead;

    public Message() {} // Firebase requires empty constructor

    public Message(String senderId, String receiverId, String message, long timestamp, boolean isRead) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    @Override
    public String toString() {
        return message;
    }
}
