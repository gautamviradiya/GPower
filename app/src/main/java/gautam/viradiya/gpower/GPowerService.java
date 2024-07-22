package gautam.viradiya.gpower;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;

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
        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
        String notificationId = remoteMessage.getData().get("id");
//        String[] time = Objects.requireNonNull(remoteMessage.getData().get("body")).split(" ");
        Log.d("Firebase FCM", "onMessageReceived: ");
        // Create the custom button intent
        Intent intent = new Intent(this, AlertActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK )
                .putExtra("title",remoteMessage.getData().get("title"))
                .putExtra("body",remoteMessage.getData().get("body"))
                .putExtra("id",remoteMessage.getData().get("id"));
//                .setAction("com.gautamviradiya.gpower.UPDATE_WIDGET")
//                .putExtra("lampStatus",notificationId)
//                .putExtra("dayNight",remoteMessage.getData().get("dayNight"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews customLayout = new RemoteViews(getPackageName(), R.layout.call_notification);
        customLayout.setOnClickPendingIntent(R.id.answer_button, getAnswerIntent());
        customLayout.setTextViewText(R.id.notification_title, remoteMessage.getData().get("title"));
        customLayout.setTextViewText(R.id.notification_subtitle,remoteMessage.getData().get("body") );

        if (notificationId.equals("power_on")) {
            customLayout.setImageViewResource(R.id.notification_lamp,R.drawable.ic_lamp_on );
            long[] pattern = {500,500,500,500,500};
            NotificationCompat.Builder powerOnNotification = new NotificationCompat.Builder(this, "power_on")
                    .setSmallIcon(R.drawable.ic_app)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_power_on))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("body"))
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setFullScreenIntent(pendingIntent, true)
                    .setVibrate(pattern)
                    .setOngoing(true)
//                    .addAction(R.drawable.ic_lamp_on,"OK",getAnswerIntent())
                    .setCustomContentView(customLayout);
//                    .setContent(customLayout);
            notificationManager.notify(1, powerOnNotification.build());
//            alert(remoteMessage);
        } else if (notificationId.equals("power_off")) {

            customLayout.setImageViewResource(R.id.notification_lamp,R.drawable.ic_lamp_red );
            NotificationCompat.Builder powerOffNotification = new NotificationCompat.Builder(this, "power_off")
                    .setSmallIcon(R.drawable.ic_app)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_power_off))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("body"))
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setFullScreenIntent(pendingIntent, true)
                    .setOngoing(true)
//                    .addAction(R.drawable.ic_lamp_on,"OK",getAnswerIntent())
                    .setCustomContentView(customLayout);

            notificationManager.notify(2, powerOffNotification.build());
//            alert(remoteMessage);
        } else if (notificationId.equals("message")) {
            NotificationCompat.Builder messageNotification = new NotificationCompat.Builder(this, "message")
                    .setSmallIcon(R.drawable.ic_app)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("body"))
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

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
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            notificationManager.notify(4, timeScheduleNotification.build());
        }
    }

    private PendingIntent getAnswerIntent() {
        Intent answerIntent = new Intent(this, CustomButtonReceiver.class);
        answerIntent.setAction("OK_BUTTON");
        return PendingIntent.getBroadcast(this, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        Log.d("FCM", "onNewToken: " + s);
    }

    public static class CustomButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("OK_BUTTON".equals(intent.getAction())) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancelAll();
            }
        }
    }

    private void alert(RemoteMessage remoteMessage) {
        Log.d("onRecive Activity", "FCM: onAlert");

        Intent activityIntent = new Intent(this, AlertActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(activityIntent);
    }

}