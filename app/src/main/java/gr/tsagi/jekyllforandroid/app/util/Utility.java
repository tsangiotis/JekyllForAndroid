package gr.tsagi.jekyllforandroid.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import gr.tsagi.jekyllforandroid.app.R;
import gr.tsagi.jekyllforandroid.app.provider.PostsContract;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGD;

/**
 * Created by tsagi on 8/9/14.
 */
public class Utility {

    private Context mContext;

    public Utility(Context context) {
        mContext = context;
    }

    /**
     * Gets the username from the private Shared preferences.
     *
     * @return username
     */
    public String getUser() {
        // We are using MODE_PRIVATE because these are sensitive data
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        // TODO: Change "user_login" to "username" without affecting current users
        return prefs.getString("user_login", "");
    }

    /**
     * Gets the token from the private Shared preferences.
     *
     * @return token
     */
    public String getToken() {
        // We are using MODE_PRIVATE because these are sensitive data
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        // TODO: Change "user_status" to "token" without affecting current users
        return prefs.getString("user_status", "");
    }

    /**
     * Gets the subdirectory of the posts if the user has set that in settings.
     *
     * @return subdirectory
     */
    public String getSubdir() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String dir = prefs.getString("posts_subdir", "");
        if(!dir.equals(""))
            return dir +"/";
        return null;
    }

    /**
     * Gets the github repo with the users Jekyll blog.
     *
     * @return repo
     */
    public String getRepo() {
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        // TODO: Change "user_repo" to "repo" without affecting current users
        return prefs.getString("user_repo", "");
    }

    /**
     * Saves the SHA-1 of the currently saved commit.
     * If it is the same, no need for sync.
     *
     * @param baseCommitSha SHA on github.
     */
    public void setBaseCommitSha(String baseCommitSha) {
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("base_commit", baseCommitSha);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            editor.apply();
        } else {
            editor.commit();
        }

    }

    /**
     * Saves the SHA-1 of the currently saved commit.
     * If it is the same, no need for sync.
     *
     * @return baseCommitSha
     */
    public String getBaseCommitSha() {
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        return prefs.getString("base_commit", "");
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return a user-friendly representation of the date.
     */
    public String getFriendlyDayString(String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Date todayDate = new Date();
        String todayStr = PostsContract.getDbDateString(todayDate);
        Date inputDate = null;
        inputDate = PostsContract.getDateFromDb(dateStr);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            String today = mContext.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(mContext.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(dateStr)));
        } else {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("MMMM dd, yyyy");

            return shortenedDateFormat.format(inputDate);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return
     */
    public String getDayName(String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            if (PostsContract.getDbDateString(todayDate).equals(dateStr)) {
                return mContext.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (PostsContract.getDbDateString(tomorrowDate).equals(
                        dateStr)) {
                    return mContext.getString(R.string.tomorrow);
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
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(String dateStr) {
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

    public String getImageUrl() {
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        return prefs.getString("AvatarUrl", "");
    }

    public Map<String,String> getJekyllRepos(){
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);

        String jsonList = prefs.getString("branchesJSON","");
        LOGD("UTILITY", jsonList);

        Gson gson = new Gson();
        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();

        return gson.fromJson(jsonList, stringStringMap);
    }

    public void setCurrentRepo(String repoName){
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_repo", repoName);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void setCurrentBranch(String branchName){
        SharedPreferences prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("repo_branch", branchName);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public Bitmap LoadImageFromWebOperations(String url) {
        try {
            Bitmap bmp = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
            Log.d("Utility", "Converted");
            return bmp;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
