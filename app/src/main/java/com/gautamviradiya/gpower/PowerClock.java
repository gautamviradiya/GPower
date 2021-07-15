package com.gautamviradiya.gpower;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;

public class PowerClock extends View {
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
        eightHourPaint.setStrokeWidth(4);
        eightHourPaint.setColor(Color.parseColor("#BEBEBE"));
        eightHourPaint.setStrokeCap(Paint.Cap.ROUND);
        eightHourPaint.setStyle(Paint.Style.STROKE);

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
        //clock
        drawHands(canvas);
        canvas.drawCircle(centerX, centerY, 7, clockCenter);

        //outer process
        drawEightHourArc(canvas,270);
        postInvalidateDelayed(500);
    }

    private void drawEightHourArc(Canvas canvas,float startAngle) {
        canvas.drawArc(outerArc, startAngle, 240, false, eightHourPaint);

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
}
