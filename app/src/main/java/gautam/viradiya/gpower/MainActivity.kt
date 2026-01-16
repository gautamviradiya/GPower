package gautam.viradiya.gpower

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() //        implements  TextToSpeech.OnInitListener
{
    private var sharedPreferences: SharedPreferences? = null
    private var sharedPreferencesEditor: SharedPreferences.Editor? = null
    var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance()
    var db: DatabaseReference =
        firebaseDatabase.getReference("/gujarat/amreli/bagasara/somnath/power")
    private var lamp: ImageView? = null
    private var dayNight: ImageView? = null
    private var settings: ImageView? = null
    private val textToSpeech: TextToSpeech? = null
    var powerClock: PowerClock? = null
    var totalPowerSupplied: TextView? = null
    var totalPowerRemaining: TextView? = null
    private var boxPowerOn: CheckBox? = null
    private var boxPowerOff: CheckBox? = null
    private var notificationManager: NotificationManager? = null
    private var audioAttributes: AudioAttributes? = null
    private val bannerAd: AdView? = null
    private var powerEndTime: String? = ""
    private var powerStartTime: String? = ""
    var onOffList: ArrayList<OnOffData> = ArrayList<OnOffData>()
    private var systemClockBR: BroadcastReceiver? = null
    private var systemIntentFilter: IntentFilter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
        systemClockBR = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                // Handle the received broadcast
                if (intent.getAction() == Intent.ACTION_TIME_TICK) {
                    // Perform your desired logic here when the system minute changes
                    calculateTotalPowerCutAndLeft()
                }
            }
        }

        // Initialize the intent filter
        systemIntentFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        registerReceiver(systemClockBR, systemIntentFilter)

        //        bannerAd = findViewById(R.id.banner_ad);
//        //ADs
//        AdRequest mainBannerAdRequest = new AdRequest.Builder().build();
//        bannerAd.loadAd(mainBannerAdRequest);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FULL_SCREEN_INTENT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.USE_FULL_SCREEN_INTENT),
                    500
                )
            }
        }

        //        textToSpeech = new TextToSpeech(this, this);
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                startActivity(intent)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
        settings!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(
                    this@MainActivity,
                    SettingsActivity::class.java
                )
            )
        })

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            public override fun onDataChange(snapshot: DataSnapshot) {
                powerStartTime = snapshot.child("startTime").getValue(String::class.java)
                Log.d("GPOWER", "onDataChange: singleValue" + powerStartTime)
                powerEndTime = snapshot.child("endTime").getValue(String::class.java)
                powerClock!!.setPowerStartTime(powerStartTime)
                powerClock!!.setPowerEndTime(powerEndTime)

                //day or night
                val day: Boolean = snapshot.child("day").getValue(Boolean::class.java) == true
                if (day) {
                    dayNight!!.setImageResource(R.drawable.ic_day)
                } else dayNight!!.setImageResource(R.drawable.ic_night)

                powerClock!!.setTotalPowerSupplyAngle(totalPowerSupply * 30.0f)

                db.child("on_off").addValueEventListener(object : ValueEventListener {
                    public override fun onDataChange(dataSnapshot: DataSnapshot) {
                        onOffList.clear()
                        for (childSnapshot in dataSnapshot.getChildren()) {
                            val onOffData: OnOffData? =
                                childSnapshot.getValue(OnOffData::class.java)
                            onOffList.add(onOffData!!)
                        }
                        calculateTotalPowerCutAndLeft()
                        powerClock!!.setOnOffList(onOffList)
                    }

                    public override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors
                        // ...
                    }
                })
            }

            public override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })

        //lamp
        db.child("status").addValueEventListener(object : ValueEventListener {
            public override fun onDataChange(snapshot: DataSnapshot) {
                val powerStatus: Boolean = snapshot.value == true
                if (powerStatus) {
                    lamp!!.setImageResource(R.drawable.ic_lamp_on)
                    powerClock!!.setPowerStatus(true)
                    //                    textToSpeech.speak("પાવર, ચાલુ", TextToSpeech.QUEUE_FLUSH, null, "");
                } else {
                    lamp!!.setImageResource(R.drawable.ic_lamp_off)
                    powerClock!!.setPowerStatus(false)
                    //                    textToSpeech.speak("પાવર, બંધ", TextToSpeech.QUEUE_FLUSH, null, "");
                }
            }

            public override fun onCancelled(error: DatabaseError) {
            }
        })

        //checkBox
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        boxPowerOn!!.setChecked(sharedPreferences!!.getBoolean("power_on", false))
        boxPowerOff!!.setChecked(sharedPreferences!!.getBoolean("power_off", false))
        boxPowerOn!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) firebaseMessaging.subscribeToTopic("power_on").addOnSuccessListener(
                OnSuccessListener { aVoid: Void? ->
                    sharedPreferencesEditor = sharedPreferences!!.edit()
                    sharedPreferencesEditor!!.putBoolean("power_on", true).apply()
                    Toast.makeText(
                        this@MainActivity,
                        "Subscribe power ON notification",
                        Toast.LENGTH_SHORT
                    ).show()
                }).addOnFailureListener(OnFailureListener { e: Exception? ->
                sharedPreferencesEditor = sharedPreferences!!.edit()
                sharedPreferencesEditor!!.putBoolean("power_on", false).apply()
                buttonView!!.setChecked(sharedPreferences!!.getBoolean("power_on", false))
                Toast.makeText(this@MainActivity, "Try after some time", Toast.LENGTH_SHORT).show()
            })
            else {
                firebaseMessaging.unsubscribeFromTopic("power_on").addOnSuccessListener(
                    OnSuccessListener { aVoid: Void? ->
                        sharedPreferencesEditor = sharedPreferences!!.edit()
                        sharedPreferencesEditor!!.putBoolean("power_on", false).apply()
                        Toast.makeText(
                            this@MainActivity,
                            "Unsubscribed power ON notification",
                            Toast.LENGTH_SHORT
                        ).show()
                    }).addOnFailureListener(OnFailureListener { e: Exception? ->
                    sharedPreferencesEditor = sharedPreferences!!.edit()
                    sharedPreferencesEditor!!.putBoolean("power_on", true).apply()
                    buttonView!!.setChecked(sharedPreferences!!.getBoolean("power_on", false))
                    Toast.makeText(this@MainActivity, "Try after some time", Toast.LENGTH_SHORT)
                        .show()
                })
            }
        })
        boxPowerOff!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) firebaseMessaging.subscribeToTopic("power_off")
                .addOnSuccessListener(object : OnSuccessListener<Void?> {
                    override fun onSuccess(aVoid: Void?) {
                        Toast.makeText(
                            this@MainActivity,
                            "Subscribe power OFF notification",
                            Toast.LENGTH_SHORT
                        ).show()
                        sharedPreferencesEditor = sharedPreferences!!.edit()
                        sharedPreferencesEditor!!.putBoolean("power_off", true).apply()
                    }
                }).addOnFailureListener(OnFailureListener { e: Exception? ->
                    sharedPreferencesEditor = sharedPreferences!!.edit()
                    sharedPreferencesEditor!!.putBoolean("power_off", false).apply()
                    buttonView!!.setChecked(sharedPreferences!!.getBoolean("power_off", false))
                    Toast.makeText(this@MainActivity, "Try after some time", Toast.LENGTH_SHORT)
                        .show()
                })
            else firebaseMessaging.unsubscribeFromTopic("power_off").addOnSuccessListener(
                OnSuccessListener { aVoid: Void? ->
                    sharedPreferencesEditor = sharedPreferences!!.edit()
                    sharedPreferencesEditor!!.putBoolean("power_off", false).apply()
                    Toast.makeText(
                        this@MainActivity,
                        "Unsubscribe power OFF notification",
                        Toast.LENGTH_SHORT
                    ).show()
                }).addOnFailureListener(OnFailureListener { e: Exception? ->
                sharedPreferencesEditor = sharedPreferences!!.edit()
                sharedPreferencesEditor!!.putBoolean("power_off", true).apply()
                buttonView!!.setChecked(sharedPreferences!!.getBoolean("power_off", false))
                Toast.makeText(this@MainActivity, "Try after some time", Toast.LENGTH_SHORT).show()
            })
        })

        audioAttributes =
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM).build()


        Log.d("permission", "onCreate: " + this.isShowOnLockScreenPermissionEnable)
        if (Build.MANUFACTURER == "Xiaomi" && !this.isShowOnLockScreenPermissionEnable!!) {
            requestShowOnLockScreenPermission()
        }
    }

    private fun initUI() {
        settings = findViewById<ImageView?>(R.id.settings)
        powerClock = findViewById<PowerClock?>(R.id.power_clock)
        lamp = findViewById<ImageView?>(R.id.lamp)
        dayNight = findViewById<ImageView?>(R.id.day_night)
        boxPowerOn = findViewById<CheckBox?>(R.id.check_box_power_on)
        boxPowerOff = findViewById<CheckBox?>(R.id.check_box_power_off)
        totalPowerSupplied = findViewById<TextView?>(R.id.supplied_power)
        totalPowerRemaining = findViewById<TextView?>(R.id.remaining_power)
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotalPowerCutAndLeft() {
        var totalOnAngle = 0.0f

        for (onOffData in onOffList) {
            val startTime = onOffData.s
            val endTime: String
            if (onOffData.e == "") {
                endTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                endTime = onOffData.e
            }

            val startAngle = calculateAngle(startTime)
            val endAngle = calculateAngle(endTime)

            val onAngle = endAngle - startAngle
            totalOnAngle += onAngle
        }

        val totalOnHours = (totalOnAngle / 30.0f) // Convert angle to hours
        val hours = (totalOnHours % 12).toInt()
        val minutes = ((totalOnAngle % 30.0f) * 2).toInt() // Convert total on angle to minutes


        val totalOnTime = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
        totalPowerSupplied!!.setText(totalOnTime + " " + getResources().getString(R.string.hours))

        //        if(hours>=getTotalPowerSupply()){
//            totalPowerRemaining.setText("00:00" + " " + getResources().getString(R.string.hours));
//            return;
//        }
        val totalRemainingHours = (this.totalPowerSupply - (totalOnHours % 8)).toInt()
        val totalRemainingMinutes =
            ((this.totalPowerSupply * 60 - totalOnAngle) % 30.0f * 2).toInt() // Calculate remaining power cut minutes
        val totalRemainingTime = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            totalRemainingHours,
            totalRemainingMinutes
        )
        totalPowerRemaining!!.setText(totalRemainingTime + " " + getResources().getString(R.string.hours))
    }

    val totalPowerSupply: Float
        get() {
            val start = LocalTime.parse(powerStartTime)
            val end = LocalTime.parse(powerEndTime)

            val duration = Duration.between(start, end)
            val hours = duration.toHours().toFloat()
            //        long minutes = duration.toMinutes() % 60;
            return hours
        }

    private val isShowOnLockScreenPermissionEnable: Boolean?
        get() {
            try {
                val manager = getSystemService(APP_OPS_SERVICE) as AppOpsManager?
                val method = AppOpsManager::class.java.getDeclaredMethod(
                    "checkOpNoThrow",
                    Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java
                )
                val result =
                    method.invoke(manager, 10020, Binder.getCallingUid(), getPackageName()) as Int
                return AppOpsManager.MODE_ALLOWED == result
            } catch (e: Exception) {
                return null
            }
        }

    private fun requestShowOnLockScreenPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
            .setMessage("Please grant 'Show on Lock Screen' and 'Display pop-up window' permissions. so you can easily get notifications.")
            .setPositiveButton("Grant", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, id: Int) {
                    val intent: Intent?
                    intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                    intent.setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    intent.putExtra("extra_pkgname", getPackageName())
                    startActivity(intent)
                }
            })
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, id: Int) {
                    // User cancelled the dialog
                }
            })
        val dialog = builder.create()
        dialog.show()
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
    class OnOffData {
        // Getters and setters for the fields
        var e: String = "" // End time
        var s: String = "" // Start time

        // Default constructor (required for Firebase deserialization)
        constructor()

        // Constructor with parameters
        constructor(s: String, e: String) {
            this.e = e
            this.s = s
        }

        constructor(s: String) {
            this.s = s
        }
    }

    private fun calculateAngle(time: String): Float {
        val parts: Array<String?> =
            time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val hour = parts[0]!!.toInt()
        val minute = parts[1]!!.toInt()
        return (hour * 30) + (minute * 0.5f) - 90
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(systemClockBR)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 200
    }
}