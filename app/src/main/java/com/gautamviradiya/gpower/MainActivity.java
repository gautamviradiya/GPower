package com.gautamviradiya.gpower;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
//        implements  TextToSpeech.OnInitListener
{
    private static final int PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 200;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
    public DatabaseReference db = firebaseDatabase.getReference("/gujarat/amreli/bagasara/somnath/power");
    private ImageView lamp, dayNight, settings;
    private TextToSpeech textToSpeech;
    PowerClock powerClock;
    private CheckBox boxPowerOn, boxPowerOff;
    private NotificationManager notificationManager;
    private AudioAttributes audioAttributes;
    private AdView bannerAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = findViewById(R.id.settings);
        powerClock = findViewById(R.id.power_clock);
        lamp = findViewById(R.id.lamp);
        dayNight = findViewById(R.id.day_night);
        boxPowerOn = findViewById(R.id.check_box_power_on);
        boxPowerOff = findViewById(R.id.check_box_power_off);
//        bannerAd = findViewById(R.id.banner_ad);
//        //ADs
//        AdRequest mainBannerAdRequest = new AdRequest.Builder().build();
//        bannerAd.loadAd(mainBannerAdRequest);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FULL_SCREEN_INTENT)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.USE_FULL_SCREEN_INTENT},
                        500);
            }
        }

//        textToSpeech = new TextToSpeech(this, this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
        settings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        db.child("on_off").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<MainActivity.OnOffData> onOffList = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    MainActivity.OnOffData onOffData = childSnapshot.getValue(MainActivity.OnOffData.class);
                    onOffList.add(onOffData);
                }
                powerClock.setOnOffList(onOffList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                // ...
            }
        });

        db.child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                powerClock.setPowerStartTime(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        db.child("endTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                powerClock.setPowerEndTime(snapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
        //lamp
        db.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean powerStatus = snapshot.getValue(Boolean.class);
                if (powerStatus) {
                    lamp.setImageResource(R.drawable.ic_lamp_on);
                    powerClock.setPowerStatus(true);
//                    textToSpeech.speak("પાવર, ચાલુ", TextToSpeech.QUEUE_FLUSH, null, "");
                } else {
                    lamp.setImageResource(R.drawable.ic_lamp_off);
                    powerClock.setPowerStatus(false);
//                    textToSpeech.speak("પાવર, બંધ", TextToSpeech.QUEUE_FLUSH, null, "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //day or night
        db.child("day").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean day = snapshot.getValue(Boolean.class);
                if (day) {
                    dayNight.setImageResource(R.drawable.ic_day);
                } else dayNight.setImageResource(R.drawable.ic_night);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //checkBox
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        boxPowerOn.setChecked(sharedPreferences.getBoolean("power_on", false));
        boxPowerOff.setChecked(sharedPreferences.getBoolean("power_off", false));

        boxPowerOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                firebaseMessaging.subscribeToTopic("power_on").addOnSuccessListener(aVoid -> {
                    sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putBoolean("power_on", true).apply();
                    Toast.makeText(MainActivity.this, "Subscribe power ON notification", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putBoolean("power_on", false).apply();
                    buttonView.setChecked(sharedPreferences.getBoolean("power_on", false));
                    Toast.makeText(MainActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                });
            else {
                firebaseMessaging.unsubscribeFromTopic("power_on").addOnSuccessListener(aVoid -> {
                    sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putBoolean("power_on", false).apply();
                    Toast.makeText(MainActivity.this, "Unsubscribed power ON notification", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putBoolean("power_on", true).apply();
                    buttonView.setChecked(sharedPreferences.getBoolean("power_on", false));
                    Toast.makeText(MainActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                });
            }
        });

        boxPowerOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                firebaseMessaging.subscribeToTopic("power_off").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Subscribe power OFF notification", Toast.LENGTH_SHORT).show();
                        sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putBoolean("power_off", true).apply();
                    }
                }).addOnFailureListener(e -> {
                    sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putBoolean("power_off", false).apply();
                    buttonView.setChecked(sharedPreferences.getBoolean("power_off", false));
                    Toast.makeText(MainActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                });
            else
                firebaseMessaging.unsubscribeFromTopic("power_off").addOnSuccessListener(aVoid -> {
                    sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putBoolean("power_off", false).apply();
                    Toast.makeText(MainActivity.this, "Unsubscribe power OFF notification", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putBoolean("power_off", true).apply();
                    buttonView.setChecked(sharedPreferences.getBoolean("power_off", false));
                    Toast.makeText(MainActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                });
        });

        audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM).build();

        Log.d("permission", "onCreate: " + isShowOnLockScreenPermissionEnable());
        if (Build.MANUFACTURER.equals("Xiaomi") && !isShowOnLockScreenPermissionEnable()) {
            requestShowOnLockScreenPermission();
        }
    }

    private Boolean isShowOnLockScreenPermissionEnable() {
        try {
            AppOpsManager manager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            Method method = AppOpsManager.class.getDeclaredMethod("checkOpNoThrow",
                    int.class, int.class, String.class);
            int result = (int) method.invoke(manager, 10020, Binder.getCallingUid(), getPackageName());
            return AppOpsManager.MODE_ALLOWED == result;
        } catch (Exception e) {
            return null;
        }
    }

    private void requestShowOnLockScreenPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required")
                .setMessage("Please grant 'Show on Lock Screen' and 'Display pop-up window' permissions. so you can easily get notifications.")
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent;
                            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            intent.putExtra("extra_pkgname", getPackageName());
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

//    @Override
//    public void onInit(int status) {
//        if (status == TextToSpeech.SUCCESS) {
//            int result = textToSpeech.setLanguage(new Locale("gu", "IND", "variant"));
//            textToSpeech.setPitch(0.9f);
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(this, "Speak fail", Toast.LENGTH_SHORT).show();
//        }
//    }

    static class OnOffData {
        private String e=""; // End time
        private String s=""; // Start time

        // Default constructor (required for Firebase deserialization)
        public OnOffData() {
        }

        // Constructor with parameters
        public OnOffData(String s, String e) {
            this.e = e;
            this.s = s;
        }
        public OnOffData(String s) {
            this.s = s;
        }



        // Getters and setters for the fields
        public String getE() {
            return e;
        }

        public void setE(String e) {
            this.e = e;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }
    }

}
