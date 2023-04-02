package com.gautamviradiya.gpower;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AlertActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line to show the activity even when the screen is locked
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
//                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
//                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
//                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.activity_alert);
        Log.d("Aleart Activity", "onCreate: ");
    }
}
