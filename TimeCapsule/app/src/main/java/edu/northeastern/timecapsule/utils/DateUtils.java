package edu.northeastern.timecapsule.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handy helpers for date and time — feel free to use these if you need to
 * display or compare Firestore Timestamps anywhere in the app.
 */
public class DateUtils {

    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());

    /** Converts a Firestore Timestamp to a readable string, e.g. "Mar 25, 2026 at 10:00 AM" */
    public static String timestampToDisplay(Timestamp timestamp) {
        if (timestamp == null) return "";
        return DISPLAY_FORMAT.format(timestamp.toDate());
    }

    /** Returns true if the capsule's unlock time has passed */
    public static boolean isUnlocked(Timestamp unlockTime) {
        if (unlockTime == null) return false;
        return unlockTime.toDate().before(new Date()) || unlockTime.toDate().equals(new Date());
    }
}
