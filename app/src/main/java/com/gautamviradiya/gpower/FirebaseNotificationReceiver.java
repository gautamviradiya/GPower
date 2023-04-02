package com.gautamviradiya.gpower;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import androidx.legacy.content.WakefulBroadcastReceiver;

public class FirebaseNotificationReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = FirebaseNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the intent contains a notification message
        if (intent.getExtras() != null && intent.getExtras().containsKey("notification")) {
            // Wake up the device's screen
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP, "FirebaseNotificationReceiver:wakeLock");
            wakeLock.acquire();

            // Disable the keyguard
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(TAG);
            keyguardLock.disableKeyguard();

            // Launch the MyActivity
            Intent activityIntent = new Intent(context, AlertActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(activityIntent);

            // Release the wake lock and re-enable the keyguard
            wakeLock.release();
            keyguardLock.reenableKeyguard();
        }
    }
}
