package lk.graynode.mobifit.entity;

public class StepData {
    private int steps;
    private float caloriesBurned;
    private String date;

    public StepData(int steps, float caloriesBurned, String date) {
        this.steps = steps;
        this.caloriesBurned = caloriesBurned;
        this.date = date;
    }

    // Getters and setters
    public int getSteps() { return steps; }
    public float getCaloriesBurned() { return caloriesBurned; }
    public String getDate() { return date; }
}
