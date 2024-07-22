package gautam.viradiya.gpower;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;
    FirebaseMessaging firebaseMessaging;
    ImageButton btnBack;
    SoundPool soundPool;
    String[] powerOnRingtoneList = new String[]{"None", "Call Ringtone", "Water pipe"};
    int onSound1, onSound2;
    String[] powerOffRingtoneList = new String[]{"None", "Power off"};
    int powerOnRingtoneIndex = 0, powerOffRingtoneIndex = 0;
    TextView powerOnSelectedRingtone, powerOffSelectedRingtone;
    LinearLayout powerOnRingtone, powerOffRingtone;
    Switch switchTimeSchedule, switchPGVCLMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseMessaging = FirebaseMessaging.getInstance();
        btnBack = findViewById(R.id.btn_back);
        powerOnRingtone = findViewById(R.id.power_on_ringtone_layout);
        powerOffRingtone = findViewById(R.id.power_off_ringtone_layout);
        switchTimeSchedule = findViewById(R.id.switch_time_schedule);
        switchPGVCLMessage = findViewById(R.id.switch_message);
        powerOnSelectedRingtone = findViewById(R.id.power_on_selected_ringtone);
        powerOffSelectedRingtone = findViewById(R.id.power_off_selected_ringtone);


        //onSound1 = soundPool.load(SettingsActivity.this, R.raw.water_drains_in_pipe, 1);
        //onSound2 = soundPool.load(SettingsActivity.this,R.raw.water_drains_in_pipe,1);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        switchPGVCLMessage.setChecked(sharedPreferences.getBoolean("pgvcl_message", false));
        switchTimeSchedule.setChecked(sharedPreferences.getBoolean("time_schedule", false));
        powerOnSelectedRingtone.setText(sharedPreferences.getString("on_ringtone1", "default"));
        powerOffSelectedRingtone.setText(sharedPreferences.getString("power_off_ringtone", "default"));

        powerOnRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MaterialAlertDialogBuilder(SettingsActivity.this).setTitle("Power ON ringtone")
                        .setSingleChoiceItems(powerOnRingtoneList, powerOnRingtoneIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        powerOnRingtoneIndex = 0;
                                        break;
                                    case 1:
                                        powerOnRingtoneIndex = 1;
                                        soundPool.play(onSound1,1,1,0,0,1);
                                        break;
                                    case 2:
                                        powerOnRingtoneIndex = 2;
                                        soundPool.play(onSound2,1,1,0,0,1);

                                }
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sharedPreferencesEditor = sharedPreferences.edit();
                                sharedPreferencesEditor.putString("on_ringtone1",powerOnRingtoneList[powerOnRingtoneIndex]).apply();
                                powerOnSelectedRingtone.setText(powerOnRingtoneList[powerOnRingtoneIndex]);
                                soundPool.autoPause();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                soundPool.autoPause();
                            }
                        }).show();
            }
        });

        powerOffRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(SettingsActivity.this).setTitle("Power OFF ringtone")
                        .setSingleChoiceItems(powerOffRingtoneList, powerOffRingtoneIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        powerOffRingtoneIndex = 0;
                                        break;
                                    case 1:
                                        powerOffRingtoneIndex = 1;
                                        break;
                                }
                            }
                        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        powerOffSelectedRingtone.setText(powerOffRingtoneList[powerOffRingtoneIndex]);
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
            }
        });

        switchPGVCLMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    firebaseMessaging.subscribeToTopic("pgvcl_message").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("pgvcl_message", true).apply();
                            Toast.makeText(SettingsActivity.this, "Subscribe PGVCL message notification", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("pgvcl_message", false).apply();
                            buttonView.setChecked(sharedPreferences.getBoolean("pgvcl_message", false));
                            Toast.makeText(SettingsActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    firebaseMessaging.unsubscribeFromTopic("pgvcl_message").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("pgvcl_message", false).apply();
                            Toast.makeText(SettingsActivity.this, "Unsubscribed PGVCL message notification", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("pgvcl_message", true).apply();
                            buttonView.setChecked(sharedPreferences.getBoolean("pgvcl_message", false));
                            Toast.makeText(SettingsActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });
        switchTimeSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    firebaseMessaging.subscribeToTopic("time_schedule").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("time_schedule", true).apply();
                            Toast.makeText(SettingsActivity.this, "Subscribed Time schedule notification", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("time_schedule", false).apply();
                            buttonView.setChecked(sharedPreferences.getBoolean("time_schedule", false));
                            Toast.makeText(SettingsActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {

                    firebaseMessaging.unsubscribeFromTopic("time_schedule").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("time_schedule", false).apply();
                            Toast.makeText(SettingsActivity.this, "Unsubscribed time schedule notification ", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putBoolean("time_schedule", true).apply();
                            buttonView.setChecked(sharedPreferences.getBoolean("time_schedule", false));
                            Toast.makeText(SettingsActivity.this, "Try after some time", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }
}
