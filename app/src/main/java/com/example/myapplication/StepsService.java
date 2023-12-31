package com.example.myapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StepsService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    public static final String CHANNEL_ID = "StepsServiceChannel";
    public static final int NOTIF_ID = 101;
    private float totalSteps = 0f;

    private float previousTotalSteps = 0f;
    private float lastSaved = 0f;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean firstSave = true;
    private static StepsService instance = null;
    private MyDatabaseHelper myDatabaseHelper;
    private String firstStepTime;
    private String lastStepTime;
    private boolean firstOpening = true;
    private final Runnable remindByTime = new Runnable() {
        @Override
        public void run() {
            // get the "remind every minute" field from settings table
            int remindTime = myDatabaseHelper.getLastDataFromColumn(MyDatabaseHelper.TABLE_NAME3, MyDatabaseHelper.COLUMN_REMINDMINS);
            // if goal is not reached send notification
            if(MainActivity.getTotalWaterConsumed() < myDatabaseHelper.getLastDataFromColumn(MyDatabaseHelper.TABLE_NAME3, MyDatabaseHelper.COLUMN_DAILYINTAKE)){
                addNotification(remindTime + " minute(s) has passed, you should drink water.");
            }
            handler.postDelayed(this, remindTime * 60 * 1000);
        }
    };
    @Override
    public void onCreate(){
        // get step sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        instance = this;
        myDatabaseHelper = MyDatabaseHelper.getInstance(this);
        firstStepTime = MainActivity.getFormattedDate();
        // for time notification
        remindByTime.run();
        // this alarm for saving steps
        scheduleAlarm();

    }
    // to stop current notification timer and restart
    public void stopAndRestartRunnable() {
        int remindMinute=myDatabaseHelper.getLastDataFromColumn(MyDatabaseHelper.TABLE_NAME3, MyDatabaseHelper.COLUMN_REMINDMINS);
        handler.removeCallbacks(remindByTime);
        handler.postDelayed(remindByTime, remindMinute * 60 * 1000);
    }
    public static StepsService getInstance() {
        return instance;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // whenever the user start walking this function triggered
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.equals(stepSensor)){
            totalSteps = (int) sensorEvent.values[0];
            // USE previousTotalSteps to send notification every X steps
            int currentSteps = (int) (totalSteps - previousTotalSteps);
            // reset the sensor on the first launch
            if(firstOpening){
                currentSteps = 0;
                firstOpening = false;
                previousTotalSteps = totalSteps;
            }
            // get setting
            int remindSteps = myDatabaseHelper.getLastDataFromColumn(MyDatabaseHelper.TABLE_NAME3, MyDatabaseHelper.COLUMN_REMINDSTEP);
            // To notify, get difference between records
            if(currentSteps > remindSteps){

                // Daily goal reached?
                if(MainActivity.getTotalWaterConsumed() < myDatabaseHelper.getLastDataFromColumn(MyDatabaseHelper.TABLE_NAME3, MyDatabaseHelper.COLUMN_DAILYINTAKE)){
                    // Control the water intake between first step and last step
                    lastStepTime = MainActivity.getFormattedDate();
                    Long lastRecordedWaterIntake = Long.parseLong(myDatabaseHelper.getLastRecordDateTime());

                    long mlPerStep = myDatabaseHelper.getLastDataFromColumn(MyDatabaseHelper.TABLE_NAME3, MyDatabaseHelper.COLUMN_DAILYINTAKE) / myDatabaseHelper.getLastDataFromColumn(MyDatabaseHelper.TABLE_NAME3, MyDatabaseHelper.COLUMN_DAILYSTEP);
                    long shouldConsume = mlPerStep * remindSteps;
                    long consumed = myDatabaseHelper.getConsumedWaterBetweenTwoDateTimes(firstStepTime, lastStepTime);
                    // Did user drink water while taking steps?
                    if(consumed < shouldConsume){
                        addNotification("You have taken " + currentSteps + " step(s) and consumed " + consumed + " ml, you should drink water.");
                    }
                    firstStepTime = lastStepTime;
                }

                // save
                previousTotalSteps = totalSteps;
            }
            // showing user
            updateNotification(currentSteps);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Steps Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class); // Assuming you want to go to MainActivity when clicking the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        // start notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter")
                .setContentText("0")
                .setSmallIcon(R.drawable.ic_steps) // Replace this with your icon
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIF_ID, notification);

        // Register the sensor listener
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }
    // to change step counter value on the screen
    private void updateNotification(int currentSteps) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter")
                .setContentText("Steps: " + currentSteps)
                .setSmallIcon(R.drawable.ic_steps) // Make sure you have this icon in your drawable folder
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIF_ID, notification);
    }

    // to save steps schedule alarm for every 30 minutes. the alarm is going to be received in ALarmReceiver
    public void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Log.i("ALERT", "ALERT HAS BEEN SET");

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("SAVE_STEPS");
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

        long repeatInterval = 30 * 60 * 1000;
        // used elapsedtime to make independent from local time
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;


        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime, pendingIntent);
            }
            else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }

    public float getTotalSteps() {
        return totalSteps;
    }

    public void setLastSaved(float lastSaved) {
        this.lastSaved = lastSaved;
    }

    public float getLastSaved() {
        return lastSaved;
    }

    public boolean isFirstSave() {
        return firstSave;
    }

    public void setFirstSave(boolean firstSave) {
        this.firstSave = firstSave;
    }

    // TODO fix redundancy
    // to send notification
    private void addNotification(String message) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// The id of the channel.
        String id = "my_channel_02";

// The user-visible name of the channel.
        CharSequence name = "abcd";

// The user-visible description of the channel.
        String description = "abcd";

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(id, name,importance);
            mChannel.setDescription(description);

            mChannel.enableLights(true);
// Sets the notification light color for notifications posted to this
// channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);

            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            mNotificationManager.createNotificationChannel(mChannel);
        }

// Configure the notification channel.

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.circle) //set icon for notification
                        .setContentTitle("DrinkWater") //set title of notification
                        .setContentText(message)//this is notification message
                        .setAutoCancel(true) // makes auto cancel of notification
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT).setChannelId(id); //set priority of notification


        Intent notificationIntent = new Intent(this, NotificationView.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //notification message will get at NotificationView
        notificationIntent.putExtra("message", "This is a notification message");
        PendingIntent notifyPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notifyPendingIntent = PendingIntent.getBroadcast(
                    getApplication(),
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            notifyPendingIntent = PendingIntent.getBroadcast(
                    getApplication(),
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        PendingIntent pendingIntent = notifyPendingIntent;
        builder.setContentIntent(pendingIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
