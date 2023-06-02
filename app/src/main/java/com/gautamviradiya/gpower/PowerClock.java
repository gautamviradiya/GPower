package com.gautamviradiya.gpower;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class PowerClock extends View {

    public DatabaseReference db = FirebaseDatabase.getInstance().getReference("/gujarat/amreli/bagasara/somnath/power");
    private float height, width, minimum, radius;
    private float centerX;
    private float centerY;
    private float hour;
    private float minute;
    private float second;
    private double angle;
    private int secondHandSize;
    private int minuteHandSize;
    private int hourHandSize;
    private RectF outerArc = new RectF();
    Paint clockCenter = new Paint();
    Paint secondHandPaint = new Paint();
    Paint minuteHandPaint = new Paint();
    Paint hourHandPaint = new Paint();
    Paint eightHourPaint = new Paint();
    Paint powerOffHourPaint = new Paint();
    Paint powerOnHourPaint = new Paint();

    boolean powerStatus = false;
    String powerStartTime = "12:00";
    String powerEndTime = "12:00";

    ArrayList<MainActivity.OnOffData> onOffList = new ArrayList<>();
    public void setOnOffList(ArrayList<MainActivity.OnOffData> onOff){
        this.onOffList = onOff;
    }

    public PowerClock(Context context) {
        super(context);
    }

    public PowerClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        clockCenter.setAntiAlias(true);
        clockCenter.setColor(Color.parseColor("#58C9D4"));
        angle = (float) ((Math.PI / 30) - (Math.PI / 2));
        //second
        secondHandPaint.setColor(Color.parseColor("#58C9D4"));
        secondHandPaint.setStyle(Paint.Style.STROKE);
        secondHandPaint.setStrokeWidth(1);
        secondHandPaint.setAntiAlias(true);
        secondHandPaint.setStrokeCap(Paint.Cap.ROUND);
        //minute
        minuteHandPaint.setColor(Color.parseColor("#C9C9C9"));
        minuteHandPaint.setStyle(Paint.Style.STROKE);
        minuteHandPaint.setStrokeWidth(3);
        minuteHandPaint.setAntiAlias(true);
        minuteHandPaint.setStrokeCap(Paint.Cap.ROUND);
        //hour
        hourHandPaint.setColor(Color.parseColor("#60FEAE"));
        hourHandPaint.setStyle(Paint.Style.STROKE);
        hourHandPaint.setAntiAlias(true);
        hourHandPaint.setStrokeWidth(5);
        hourHandPaint.setStrokeCap(Paint.Cap.ROUND);

        //8 hour arc
        eightHourPaint.setAntiAlias(true);
        eightHourPaint.setStrokeWidth(16);
        eightHourPaint.setColor(Color.parseColor("#BEBEBE"));
        eightHourPaint.setStrokeCap(Paint.Cap.ROUND);
        eightHourPaint.setStyle(Paint.Style.STROKE);

        //power off arc
        powerOffHourPaint.setAntiAlias(true);
        powerOffHourPaint.setStrokeWidth(16);
        powerOffHourPaint.setColor(Color.parseColor("#FF0000"));
        powerOffHourPaint.setStrokeCap(Paint.Cap.ROUND);
        powerOffHourPaint.setStyle(Paint.Style.STROKE);

        //Power on arc
        powerOnHourPaint.setAntiAlias(true);
        powerOnHourPaint.setStrokeWidth(16);
        powerOnHourPaint.setColor(Color.parseColor("#00FF00"));
        powerOnHourPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
       /* width = getWidth();
        height = getHeight();*/
        centerX = width / 2;
        centerY = height / 2;
        minimum = Math.min(width, height);
        radius = minimum / 2;
        secondHandSize = (int) (radius - radius / 1.95);
        minuteHandSize = (int) (radius - radius / 1.75);
        hourHandSize = (int) (radius - radius / 1.5);

        outerArc.left = (int) (radius - radius / 1.2);
        outerArc.top = (int) (radius - radius / 1.2);
        outerArc.right = (int) (radius + radius / 1.2);
        outerArc.bottom = (int) (radius + radius / 1.2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawHands(canvas);
        canvas.drawCircle(centerX, centerY, 7, clockCenter);
        drawEightHourArc(canvas,calculateAngle(powerStartTime));
        canvas.drawArc(outerArc,calculateAngle(powerStartTime),
                calculateAngle(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) - calculateAngle(powerStartTime),false,powerOffHourPaint);

        for (MainActivity.OnOffData onOffData : onOffList) {
            String startTime = onOffData.getS();
            String endTime;
            if (onOffData.getE().equals("")) {
                endTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                endTime = onOffData.getE();
            }

            // Calculate the start and end angles based on the start and end times
            float startAngle = calculateAngle(startTime);
            float endAngle = calculateAngle(endTime);

            // Draw the arc on the canvas
            canvas.drawArc(outerArc, startAngle, endAngle - startAngle, false, powerOnHourPaint);
        }
        postInvalidateDelayed(500);
    }
    private void drawEightHourArc(Canvas canvas, float startAngle) {
        canvas.drawArc(outerArc, startAngle, 240, false, eightHourPaint);
    }
    private float calculateAngle(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return (hour * 30) + (minute * 0.5f) - 90;
    }

    private void drawHands(Canvas canvas) {
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        hour = hour > 12 ? hour - 12 : hour;
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
        drawHourHand(canvas, (hour + minute / 60.0) * 5f);
        drawMinuteHand(canvas, minute);
        drawSecondsHand(canvas, second);
    }

    private void drawHourHand(Canvas canvas, double hour) {
        angle = Math.PI * hour / 30 - Math.PI / 2;
        canvas.drawLine(centerX, centerY, (float) (centerX + Math.cos(angle) * hourHandSize)
                , (float) (centerY + Math.sin(angle) * hourHandSize), hourHandPaint);
    }

    private void drawMinuteHand(Canvas canvas, float minute) {
        angle = Math.PI * minute / 30 - Math.PI / 2;
        canvas.drawLine(centerX, centerY, (float) (centerX + Math.cos(angle) * minuteHandSize)
                , (float) (centerY + Math.sin(angle) * minuteHandSize), minuteHandPaint);
    }

    private void drawSecondsHand(Canvas canvas, float second) {
        angle = Math.PI * second / 30 - Math.PI / 2;
        canvas.drawLine(centerX, centerY, (float) (centerX + Math.cos(angle) * secondHandSize)
                , (float) (centerY + Math.sin(angle) * secondHandSize), secondHandPaint);
    }

    public void setPowerStatus(boolean powerStatus) {
        this.powerStatus = powerStatus;
        invalidate(); // Redraw the clock when the power status changes
    }
    public void setPowerStartTime(String powerStartTime) {
        this.powerStartTime = powerStartTime;
    }
    public void setPowerEndTime(String powerEndTime) {
        this.powerEndTime = powerEndTime;
    }
}
