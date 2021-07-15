package com.gautamviradiya.gpower;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class GPowerService extends FirebaseMessagingService {
    //private static final String TAG = "GPowerService";
    //DatabaseReference db = FirebaseDatabase.getInstance().getReference("/gujarat/amreli/bagasara/somnath/power");
    NotificationManagerCompat notificationManager;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        notificationManager =  NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
        String notificationId = remoteMessage.getNotification().getChannelId();
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (notificationId.equals("power_on")) {
            NotificationCompat.Builder powerOnNotification = new NotificationCompat.Builder(this, "power_on")
                    .setSmallIcon(R.drawable.ic_app)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_power_on))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentIntent(pendingIntent);
            notificationManager.notify(1, powerOnNotification.build());
           // alert();
        } else if (notificationId.equals("power_off")) {
            NotificationCompat.Builder powerOffNotification = new NotificationCompat.Builder(this, "power_off")
                    .setSmallIcon(R.drawable.ic_app)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_power_off))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentIntent(pendingIntent);
            notificationManager.notify(2, powerOffNotification.build());

        } else if (notificationId.equals("message")) {
            NotificationCompat.Builder messageNotification = new NotificationCompat.Builder(this, "message")
                    .setSmallIcon(R.drawable.ic_app)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentIntent(pendingIntent);
            notificationManager.notify(3, messageNotification.build());
            //alert();
        } else if (notificationId.equals("time_schedule")) {
            NotificationCompat.Builder timeScheduleNotification = new NotificationCompat.Builder(this, "time_schedule")
                    .setSmallIcon(R.drawable.ic_app)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("message"))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(4, timeScheduleNotification.build());
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {

    }

    private void alert() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 50, pendingIntent);
    }
}