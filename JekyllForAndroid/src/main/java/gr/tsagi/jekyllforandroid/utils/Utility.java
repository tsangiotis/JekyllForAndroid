package gr.tsagi.jekyllforandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // TODO: Change "user_repo" to "repo" without affecting current users
        return prefs.getString("user_repo", "");
    }
}
