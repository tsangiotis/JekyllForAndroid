package gr.tsagi.jekyllforandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.data.PostsContract;

/**
 * Created by tsagi on 8/9/14.
 */
public class Utility {

    /**
     * Gets the username from the private Shared preferences.
     *
     * @param context Context to use for resource localization
     * @return username
     */
    public static String getUser(Context context) {
        // We are using MODE_PRIVATE because these are sensitive data
        SharedPreferences prefs = context.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        // TODO: Change "user_login" to "username" without affecting current users
        return prefs.getString("user_login", "");
    }

    /**
     * Gets the token from the private Shared preferences.
     *
     * @param context Context to use for resource localization
     * @return token
     */
    public static String getToken(Context context) {
        // We are using MODE_PRIVATE because these are sensitive data
        SharedPreferences prefs = context.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        // TODO: Change "user_status" to "token" without affecting current users
        return prefs.getString("user_status", "");
    }

    /**
     * Gets the subdirectory of the posts if the user has set that in settings.
     *
     * @param context Context to use for resource localization
     * @return subdirectory
     */
    public static String getSubdir(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String dir = prefs.getString("posts_subdir", "");
        if(!dir.equals(""))
            return dir +"/";
        return null;
    }

    /**
     * Gets the github repo with the users Jekyll blog.
     *
     * @param context Context to use for resource localization
     * @return repo
     */
    public static String getRepo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        // TODO: Change "user_repo" to "repo" without affecting current users
        return prefs.getString("user_repo", "");
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Date todayDate = new Date();
        String todayStr = PostsContract.getDbDateString(todayDate);
        Date inputDate = PostsContract.getDateFromDb(dateStr);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateStr)));
        } else {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            return shortenedDateFormat.format(inputDate);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return
     */
    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            if (PostsContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (PostsContract.getDbDateString(tomorrowDate).equals(
                        dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    // Otherwise, the format is just the day of the week (e.g "Wednesday".
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // It couldn't process the date correctly.
            return "";
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            String monthDayString = monthDayFormat.format(inputDate);
            return monthDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
