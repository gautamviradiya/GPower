package com.gautamviradiya.gpower;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

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

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
    DatabaseReference db = firebaseDatabase.getReference("/gujarat/amreli/bagasara/somnath/power");
    private ImageView lamp, dayNight, settings;
    private TextToSpeech textToSpeech;
    private CheckBox boxPowerOn, boxPowerOff;
    private NotificationManager notificationManager;
    private AudioAttributes audioAttributes;
    private AdView bannerAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = findViewById(R.id.settings);
        lamp = findViewById(R.id.lamp);
        dayNight = findViewById(R.id.day_night);
        boxPowerOn = findViewById(R.id.check_box_power_on);
        boxPowerOff = findViewById(R.id.check_box_power_off);
        bannerAd = findViewById(R.id.banner_ad);

        //ADs
        AdRequest mainBannerAdRequest = new AdRequest.Builder().build();
        bannerAd.loadAd(mainBannerAdRequest);

        //textToSpeech = new TextToSpeech(this, this);
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
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        //lamp
        db.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean powerStatus = snapshot.getValue(Boolean.class);
                if (powerStatus) {
                    lamp.setImageResource(R.drawable.ic_lamp_on);
                    //textToSpeech.speak("પાવર, ચાલુ", TextToSpeech.QUEUE_FLUSH, null, "");
                } else {
                    lamp.setImageResource(R.drawable.ic_lamp_off);
                    //textToSpeech.speak("પાવર, બંધ", TextToSpeech.QUEUE_FLUSH, null, "");
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

        boxPowerOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    firebaseMessaging.subscribeToTopic("power_on").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("power_on", true).apply();
                            Toast.makeText(MainActivity.this, "Subscribe power ON notification", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("power_on", false).apply();
                            buttonView.setChecked(sharedPreferences.getBoolean("power_on", false));
                            Toast.makeText(MainActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                        }
                    });
                else {
                    firebaseMessaging.unsubscribeFromTopic("power_on").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("power_on", false).apply();
                            Toast.makeText(MainActivity.this, "Unsubscribed power ON notification", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("power_on", true).apply();
                            buttonView.setChecked(sharedPreferences.getBoolean("power_on", false));
                            Toast.makeText(MainActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        boxPowerOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    firebaseMessaging.subscribeToTopic("power_off").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Subscribe power OFF notification", Toast.LENGTH_SHORT).show();
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("power_off", true).apply();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("power_off", false).apply();
                            buttonView.setChecked(sharedPreferences.getBoolean("power_off", false));
                            Toast.makeText(MainActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                        }
                    });
                else
                    firebaseMessaging.unsubscribeFromTopic("power_off").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("power_off", false).apply();
                            Toast.makeText(MainActivity.this, "Unsubscribe power OFF notification", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("power_off", true).apply();
                            buttonView.setChecked(sharedPreferences.getBoolean("power_off", false));
                            Toast.makeText(MainActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM).build();
    }

}

   /* @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(new Locale("gu", "IND", "variant"));
            textToSpeech.setPitch(0.9f);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Speak fail", Toast.LENGTH_SHORT).show();
        }
    }
*/

