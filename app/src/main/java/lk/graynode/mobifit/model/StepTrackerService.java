package lk.graynode.mobifit.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import lk.graynode.mobifit.MainActivity;
import lk.graynode.mobifit.R;
import lk.graynode.mobifit.entity.StepData;

public class StepTrackerService extends Service {
    private StepTracker stepTracker;
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "StepTrackerChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        stepTracker = new StepTracker(this, new StepTracker.StepDataCallback() {
            @Override
            public void onStepDataUpdated(StepData stepData) {
                updateNotification(stepData);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification(null));
        stepTracker.startTracking();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stepTracker.stopTracking();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Tracker",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks your daily steps");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(StepData stepData) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        String contentText = stepData != null
                ? String.format("Steps: %d | Calories: %.1f", stepData.getSteps(), stepData.getCaloriesBurned())
                : "Tracking steps...";

        return new NotificationCompat.Builder(this, CHANNEL_ID)  // Changed this line
                .setContentTitle("Step Tracker Active")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.pedestrian)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification(StepData stepData) {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, createNotification(stepData));
    }
}
