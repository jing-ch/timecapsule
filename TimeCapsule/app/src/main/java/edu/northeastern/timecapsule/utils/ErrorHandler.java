package edu.northeastern.timecapsule.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Handy helpers for error handling — feel free to use these if you want a quick way
 * to show errors to the user or turn Firebase exceptions into readable messages.
 */
public class ErrorHandler {

    /** Shows a short Toast message to the user */
    public static void showError(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /** Converts a Firebase exception into a user-friendly error message */
    public static String getFirebaseErrorMessage(Exception e) {
        if (e == null) return "An unknown error occurred";
        String message = e.getMessage();
        if (message == null) return "An unknown error occurred";
        if (message.contains("email address is already in use")) return "This email is already registered";
        if (message.contains("no user record")) return "No account found with this email";
        if (message.contains("password is invalid")) return "Incorrect password";
        if (message.contains("network error")) return "Network error, please try again";
        return "Something went wrong, please try again";
    }
}
