package lk.graynode.mobifit.model;

public class Validations {

    public static boolean isEmailValid(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public static Boolean isPasswordValid(String password) {
        // Regex Explanation:
        // ^                  : Start of the string
        // (?=.*[0-9])        : At least one digit
        // (?=.*[a-z])        : At least one lowercase letter
        // (?=.*[A-Z])        : At least one uppercase letter
        // (?=.*[@#$%^&+=])   : At least one special character (@#$%^&+=)
        // (?=\\S+$)          : No whitespace allowed
        // .{8,}              : At least 8 characters long
        // $                  : End of the string
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

        // Return true if password matches the regex, false otherwise
        return password != null && password.matches(regex);
    }

    public static boolean isDouble(String text) {
        return text.matches("^\\d+(\\.\\d{2})?$");
    }

    public static boolean isInteger(String text) {
        return text.matches("^\\d+$");
    }

    public static boolean isMobileNumberValid(String text) {
        return text.matches("^07[01245678]{1}[0-9]{7}$");
    }
}
