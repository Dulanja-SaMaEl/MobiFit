package lk.graynode.mobifit.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import lk.graynode.mobifit.entity.StepData;

public class StepTracker implements SensorEventListener {
    private static final String TAG = "StepTracker";
    private static final float CALORIES_PER_STEP = 0.04f;
    private static final String LAST_BOOT_STEP_COUNT = "last_boot_step_count";
    private static final String CURRENT_DAY_STEPS = "current_day_steps";
    private static final String LAST_TOTAL_STEP_COUNT = "last_total_step_count";
    private static final String REFERENCE_STEP_COUNT = "reference_step_count";

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int currentSteps = 0;
    private int initialStepCount = 0;
    private StepDataCallback callback;
    private Context context;
    private SharedPreferences prefs;
    private boolean isFirstSensorReading = true;

    public interface StepDataCallback {
        void onStepDataUpdated(StepData stepData);
    }

    public StepTracker(Context context, StepDataCallback callback) {
        this.context = context;
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        prefs = context.getSharedPreferences("StepData", Context.MODE_PRIVATE);

        checkAndResetDailySteps();
    }

    public void startTracking() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e(TAG, "Step sensor not available on this device");
        }
    }

    public void stopTracking() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        // Save the current state when tracking stops
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        saveStepData(new StepData(currentSteps, calculateCalories(currentSteps), currentDate));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalStepsSinceBoot = (int) event.values[0];
            Log.d(TAG, "Raw sensor value: " + totalStepsSinceBoot);

            // First reading after app start?
            if (isFirstSensorReading) {
                isFirstSensorReading = false;

                // Check for device reboot
                int lastBootStepCount = prefs.getInt(LAST_BOOT_STEP_COUNT, -1);
                if (lastBootStepCount == -1 || totalStepsSinceBoot < lastBootStepCount) {
                    // First launch or reboot detected
                    Log.d(TAG, "First launch or reboot detected");
                    prefs.edit().putInt(LAST_BOOT_STEP_COUNT, totalStepsSinceBoot).apply();

                    // Set reference point for this boot session
                    int referenceStepCount = prefs.getInt(REFERENCE_STEP_COUNT, 0);
                    if (referenceStepCount == 0) {
                        // First ever launch
                        prefs.edit().putInt(REFERENCE_STEP_COUNT, totalStepsSinceBoot).apply();
                        initialStepCount = totalStepsSinceBoot;
                    } else {
                        // After reboot, load previous day's steps
                        initialStepCount = totalStepsSinceBoot - currentSteps;
                    }
                } else {
                    // Normal app start (no reboot)
                    // Calculate base step count to maintain today's progress
                    initialStepCount = totalStepsSinceBoot - currentSteps;
                    Log.d(TAG, "Normal start - setting initial count to: " + initialStepCount);
                }

                // Save the latest reference
                prefs.edit()
                        .putInt(LAST_TOTAL_STEP_COUNT, totalStepsSinceBoot)
                        .putInt(REFERENCE_STEP_COUNT, initialStepCount)
                        .apply();
            }

            // Calculate today's steps
            currentSteps = totalStepsSinceBoot - initialStepCount;

            // Ensure we never have negative steps
            if (currentSteps < 0) {
                Log.d(TAG, "Negative step count detected, resetting");
                initialStepCount = totalStepsSinceBoot;
                currentSteps = 0;

                // Update the reference
                prefs.edit().putInt(REFERENCE_STEP_COUNT, initialStepCount).apply();
            }

            // Save current total for reference
            prefs.edit().putInt(LAST_TOTAL_STEP_COUNT, totalStepsSinceBoot).apply();

            float caloriesBurned = calculateCalories(currentSteps);
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Log.d(TAG, "Current steps: " + currentSteps + ", Initial: " + initialStepCount);

            StepData stepData = new StepData(currentSteps, caloriesBurned, currentDate);
            callback.onStepDataUpdated(stepData);
            saveStepData(stepData);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for step counter
    }

    private float calculateCalories(int steps) {
        return steps * CALORIES_PER_STEP;
    }

    private void saveStepData(StepData data) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(data.getDate() + "_steps", data.getSteps());
        editor.putFloat(data.getDate() + "_calories", data.getCaloriesBurned());
        editor.putString("last_saved_date", data.getDate());
        editor.putInt(CURRENT_DAY_STEPS, data.getSteps());
        editor.apply();

        Log.d(TAG, "Saved step data: " + data.getSteps() + " steps on " + data.getDate());
    }

    private void checkAndResetDailySteps() {
        String lastSavedDate = prefs.getString("last_saved_date", "");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Log.d(TAG, "Checking daily reset. Last: " + lastSavedDate + ", Current: " + currentDate);

        if (lastSavedDate.isEmpty() || !lastSavedDate.equals(currentDate)) {
            // New day detected or first launch
            Log.d(TAG, "New day detected or first launch, resetting step count");

            // Start fresh for today
            currentSteps = 0;

            // The initialStepCount will be properly set when we get the first sensor reading
            // We'll just load the current step count for display purposes

            // Update preferences for the new day
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(CURRENT_DAY_STEPS, 0);
            editor.putString("last_saved_date", currentDate);
            // Don't update initialStepCount yet - we'll do that when we get sensor data
            editor.apply();

            Log.d(TAG, "Day reset complete");
        } else {
            // Same day, load current progress
            currentSteps = prefs.getInt(CURRENT_DAY_STEPS, 0);
            Log.d(TAG, "Same day, loaded current steps: " + currentSteps);
        }
    }

    public ArrayList<StepData> getWeeklyData() {
        ArrayList<StepData> weeklyData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Get current date
        Date currentDate = new Date();

        for (int i = 6; i >= 0; i--) {
            // Calculate the date i days ago
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_YEAR, -i);

            String date = dateFormat.format(calendar.getTime());
            int steps = prefs.getInt(date + "_steps", 0);
            float calories = prefs.getFloat(date + "_calories", 0f);

            weeklyData.add(new StepData(steps, calories, date));
        }

        return weeklyData;
    }

    public int getCurrentSteps() {
        return currentSteps;
    }

    public float getCurrentCalories() {
        return calculateCalories(currentSteps);
    }
}