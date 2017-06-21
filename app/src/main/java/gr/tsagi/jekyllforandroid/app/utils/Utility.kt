package gr.tsagi.jekyllforandroid.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import gr.tsagi.jekyllforandroid.app.R
import gr.tsagi.jekyllforandroid.app.data.PostsContract
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 8/9/14
\* Time: 15:14
\*/
class Utility(private val mContext: Context) {

    /**
     * Gets the username from the private Shared preferences.

     * @return username
     */
    // We are using MODE_PRIVATE because these are sensitive data
    // TODO: Change "user_login" to "username" without affecting current users
    val user: String
        get() {
            val prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                    Context.MODE_PRIVATE)
            return prefs.getString("user_login", "")
        }

    /**
     * Gets the token from the private Shared preferences.

     * @return token
     */
    // We are using MODE_PRIVATE because these are sensitive data
    // TODO: Change "user_status" to "token" without affecting current users
    val token: String
        get() {
            val prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                    Context.MODE_PRIVATE)
            return prefs.getString("user_status", "")
        }

    /**
     * Gets the github repo with the users Jekyll blog.

     * @return repo
     */
    // TODO: Change "user_repo" to "repo" without affecting current users
    val repo: String
        get() {
            val prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                    Context.MODE_PRIVATE)
            return prefs.getString("user_repo", "")
        }

    /**
     * Saves the SHA-1 of the currently saved commit.
     * If it is the same, no need for sync.

     * @return baseCommitSha
     */
    /**
     * Saves the SHA-1 of the currently saved commit.
     * If it is the same, no need for sync.

     * @param baseCommitSha SHA on github.
     */
    var baseCommitSha: String
        get() {
            val prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                    Context.MODE_PRIVATE)
            return prefs.getString("base_commit", "")
        }
        @SuppressLint("ObsoleteSdkInt")

        set(baseCommitSha) {
            val prefs = mContext.getSharedPreferences("gr.tsagi.jekyllforandroid",
                    Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("base_commit", baseCommitSha)
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                editor.apply()
            } else {
                editor.commit()
            }

        }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.

     * @param dateStr The db formatted date string, expected to be of the form specified
     * *                in Utility.DATE_FORMAT
     * *
     * @return a user-friendly representation of the date.
     */
    fun getFriendlyDayString(dateStr: String): String {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        val todayDate = Date()
        val todayStr = PostsContract.getDbDateString(todayDate)
        val inputDate = PostsContract.getDateFromDb(dateStr)

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr == dateStr) {
            val today = mContext.getString(R.string.today)
            val formatId = R.string.format_full_friendly_date
            return String.format(mContext.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(dateStr)))
        } else {
            @SuppressLint("SimpleDateFormat")
            val shortenedDateFormat = SimpleDateFormat("MMMM dd, yyyy")

            return shortenedDateFormat.format(inputDate)
        }
    }

    companion object {

        // Format used for storing dates in the database.  ALso used for converting those strings
        // back into date objects for comparison/processing.
        val DATE_FORMAT = "yyyyMMdd"

        /**
         * Converts db date format to the format "Month day", e.g "June 24".
         * @param dateStr The db formatted date string, expected to be of the form specified
         * *                in Utility.DATE_FORMAT
         * *
         * @return The day in the form of a string formatted "December 6"
         */
        fun getFormattedMonthDay(dateStr: String): String? {
            @SuppressLint("SimpleDateFormat")
            val dbDateFormat = SimpleDateFormat(Utility.DATE_FORMAT)
            try {
                val inputDate = dbDateFormat.parse(dateStr)
                @SuppressLint("SimpleDateFormat")
                val monthDayFormat = SimpleDateFormat("MMMM dd")
                val monthDayString = monthDayFormat.format(inputDate)
                return monthDayString
            } catch (e: ParseException) {
                e.printStackTrace()
                return null
            }

        }
    }
}
