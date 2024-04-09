package com.example.xpensinator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "MyChannel";
    private static final String CHANNEL_NAME = "My Channel";
    private static final String CHANNEL_DESCRIPTION = "My Channel Description";
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void createNotification() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Budget Exceeded")
                .setContentText("You have exceeded your budget for this month.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(0, builder.build());
    }
}
