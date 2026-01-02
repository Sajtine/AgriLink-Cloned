package com.example.loginappclone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "foreground_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification() != null ?
                remoteMessage.getNotification().getTitle() : "Notification";
        String body = remoteMessage.getNotification() != null ?
                remoteMessage.getNotification().getBody() : "";

        // Display the notification immediately
        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.enableVibration(true);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // heads-up
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);   // sound + vibration

        manager.notify(0, builder.build());
    }
}

