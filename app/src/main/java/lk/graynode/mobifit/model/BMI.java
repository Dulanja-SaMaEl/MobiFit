package lk.graynode.mobifit.model;

public class BMI {
    private double weight; // in kg
    private double height; // in cm

    // Constructor
    public BMI(double weight, double height) {
        this.weight = weight;
        this.height = height;
    }

    // Getter and Setter methods
    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    // Method to calculate BMI (Height converted from cm to meters)
    public double calculateBMI() {
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be greater than zero");
        }
        double heightInMeters = height / 100.0; // Convert cm to meters
        return weight / (heightInMeters * heightInMeters);
    }

    // Method to get BMI category
    public String getBMICategory() {
        double bmi = calculateBMI();

        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi < 24.9) {
            return "Normal weight";
        } else if (bmi >= 25 && bmi < 29.9) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    // Override toString for easy debugging
    @Override
    public String toString() {
        return "BMI: " + String.format("%.2f", calculateBMI()) + " (" + getBMICategory() + ")";
    }
}
