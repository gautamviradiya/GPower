package gautam.viradiya.gpower;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

public class GPower extends Application {
    NotificationManager notificationManager;
    AudioAttributes audioAttributes;
    SoundPool soundPool;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes).build();

            NotificationChannel powerOn = new NotificationChannel("power_on", "Power ON", NotificationManager.IMPORTANCE_HIGH);
            powerOn.setShowBadge(true);
            powerOn.setDescription("notify when power ON");
            powerOn.enableLights(true);
            powerOn.setLightColor(Color.GREEN);
            powerOn.setSound(Settings.System.DEFAULT_RINGTONE_URI, audioAttributes);
            powerOn.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(powerOn);

            NotificationChannel powerOff = new NotificationChannel("power_off", "Power OFF", NotificationManager.IMPORTANCE_HIGH);
            powerOff.setShowBadge(true);
            powerOff.enableLights(true);
            powerOff.setDescription("notify when power OFF");
            powerOff.setLightColor(Color.RED);
            powerOff.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            powerOff.setSound(Settings.System.DEFAULT_RINGTONE_URI, audioAttributes);
            notificationManager.createNotificationChannel(powerOff);

            NotificationChannel message = new NotificationChannel("message", "Power Message", NotificationManager.IMPORTANCE_DEFAULT);
            message.setShowBadge(true);
            message.enableLights(true);
            message.setLightColor(Color.RED);
            message.setDescription("power related message");
            message.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(message);

            NotificationChannel weeklyTimeSchedule = new NotificationChannel("time_schedule", "Time schedule", NotificationManager.IMPORTANCE_DEFAULT);
            weeklyTimeSchedule.setShowBadge(true);
            weeklyTimeSchedule.setDescription("weekly time schedule for power on/off");
            weeklyTimeSchedule.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(weeklyTimeSchedule);

        }
}}
