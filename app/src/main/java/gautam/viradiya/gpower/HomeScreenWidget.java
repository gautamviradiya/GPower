package gautam.viradiya.gpower;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeScreenWidget extends AppWidgetProvider {


    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Create the widget layout
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_home_screen);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference db = firebaseDatabase.getReference("/gujarat/amreli/bagasara/somnath/power");

//        // Set up the button click event
//        Intent launchIntent = new Intent(context, AlertActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
//        views.setOnClickPendingIntent(R.id.launch_button, pendingIntent);

        db.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean powerStatus = snapshot.getValue(Boolean.class);
                Log.d("widget", "onDataChange: power " +  powerStatus);

                Uri imageUri;
                if (powerStatus) {
                    Log.d("widget", "onDataChange: power on ");
                    // Create a URI for the image you want to display in your widget
                    imageUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.ic_lamp_on);
                } else {
                    imageUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.ic_lamp_off);
                }
                views.setImageViewUri(R.id.widget_lamp,imageUri);
                appWidgetManager.updateAppWidget(appWidgetIds, views);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //day or night
        db.child("day").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean day = snapshot.getValue(Boolean.class);
                Uri imageUri;
                if (day) {
                    imageUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.ic_day);
                } else {
                    imageUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.ic_night);
                }
                views.setImageViewUri(R.id.widget_dayNight,imageUri);
                appWidgetManager.updateAppWidget(appWidgetIds, views);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
}
