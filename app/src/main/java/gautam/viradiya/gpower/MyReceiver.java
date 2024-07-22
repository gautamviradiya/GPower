package gautam.viradiya.gpower;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the data from the notification
        String myData = intent.getStringExtra("data");

        // Update the widget with the new data
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, HomeScreenWidget.class));
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_home_screen);

        views.setTextViewText(R.id.home, myData);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
}
