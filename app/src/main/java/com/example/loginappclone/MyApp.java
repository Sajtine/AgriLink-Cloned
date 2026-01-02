package com.example.loginappclone;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.pusher.pushnotifications.PushNotifications;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PushNotifications.start(this, "82e5c130-03f5-40a0-9494-22f7bc17bc27");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "pusher_beams_default",
                    "Push Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.enableVibration(true);
            channel.setDescription("Heads-up notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
