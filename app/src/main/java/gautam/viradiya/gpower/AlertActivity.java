package gautam.viradiya.gpower;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

public class AlertActivity extends AppCompatActivity {
    Button ok;
    TextView tv_title, tv_body;
    ImageView lamp;
    String id, title, body;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        turnScreenOnAndKeyguardOff(this);
        ok = findViewById(R.id.alert_ok);
        tv_title = findViewById(R.id.alert_title);
        tv_body = findViewById(R.id.alert_body);
        lamp= findViewById(R.id.alert_lamp);
        ok.setOnClickListener(v -> {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancelAll();
            finish();
        });

        Intent data= getIntent();
        id = data.getStringExtra("id");
        title = data.getStringExtra("title");
        body = data.getStringExtra("body");

        tv_title.setText(title);
        tv_body.setText(body);
        if(id.equals("power_on")){
            lamp.setImageResource(R.drawable.ic_lamp_on);
        }else if(id.equals("power_off")){
            lamp.setImageResource(R.drawable.ic_lamp_red);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnScreenOffAndKeyguardOn(this);
    }

    public void turnScreenOnAndKeyguardOff(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(true);
            activity.setTurnScreenOn(true);
        } else {
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(activity, null);
        }
    }

    public void turnScreenOffAndKeyguardOn(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(false);
            activity.setTurnScreenOn(false);
        } else {
            activity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }
    }

}
