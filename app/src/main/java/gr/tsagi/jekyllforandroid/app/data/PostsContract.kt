package gr.tsagi.jekyllforandroid.app.data

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 8/8/14
\* Time: 19:47
\*/
object PostsContract {

    private val LOG_TAG = PostsContract::class.java.simpleName

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    val CONTENT_AUTHORITY = "gr.tsagi.jekyllforandroid"

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    val BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY)

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://gr.tsagi.jekyllforandroid/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    val PATH_POSTS = "posts"
    val PATH_TAGS = "tags"
    val PATH_CATEGORIES = "categories"

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    val DATE_FORMAT = "yyyyMMdd"

    /**
     * Converts Date class to a string representation, used for easy comparison and database lookup.
     * @param date The input date
     * *
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
     */
    fun getDbDateString(date: Date): String {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        val sdf = SimpleDateFormat(DATE_FORMAT)
        return sdf.format(date)
    }

    /**
     * Converts a dateText to a long Unix time representation
     * @param dateText the input date string
     * *
     * @return the Date object
     */
    fun getDateFromDb(dateText: String): Date {
        val dbDateFormat = SimpleDateFormat(DATE_FORMAT)
        try {
            return dbDateFormat.parse(dateText)
        } catch (e: ParseException) {
            //            e.printStackTrace();
            return Date()
        }

    }

    /* Inner class that defines the table contents of the location table */
    class PostEntry : BaseColumns {
        companion object {

            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build()

            val CONTENT_TYPE =
                    "vnd.android.cursor.dir/$CONTENT_AUTHORITY/$PATH_POSTS"
            val CONTENT_ITEM_TYPE =
                    "vnd.android.cursor.item/$CONTENT_AUTHORITY/$PATH_POSTS"

            // Table name
            val TABLE_NAME = "posts"

            val COLUMN_POST_ID = "id"
            val COLUMN_TITLE = "title"
            val COLUMN_DRAFT = "draft"
            val COLUMN_DATETEXT = "date"
            val COLUMN_CONTENT = "content"


            fun buildPostUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }

            fun buildPublishedPosts(): Uri {
                return CONTENT_URI.buildUpon().appendPath("published").build()
            }

            fun buildDraftPosts(): Uri {
                return CONTENT_URI.buildUpon().appendPath("drafts").build()
            }

            fun buildPostFromId(status: String, postId: String): Uri {
                Log.d(LOG_TAG, "postId: " + postId)
                return CONTENT_URI.buildUpon().appendPath(status).appendPath(postId)
                        .build()
            }

            fun getStatusFromUri(uri: Uri): String {
                return uri.pathSegments[1]
            }

            fun getIdFromUri(uri: Uri): String {
                return uri.pathSegments[2]
            }
        }

    }

    //    public static final class TagEntry implements BaseColumns {
    //
    //        public static final Uri CONTENT_URI =
    //                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAGS).build();
    //
    //        public static final String CONTENT_TYPE =
    //                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_TAGS;
    //        public static final String CONTENT_ITEM_TYPE =
    //                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_TAGS;
    //
    //        // Table name
    //        public static final String TABLE_NAME = "tags";
    //
    //        public static final String COLUMN_TAG = "tag";
    //        public static final String COLUMN_POST_ID = "post_id";
    //
    //        public static Uri buildTagUri(long id) {
    //            return ContentUris.withAppendedId(CONTENT_URI, id);
    //        }
    //    }

    //    public static final class CategoryEntry implements BaseColumns {
    //
    //        public static final Uri CONTENT_URI =
    //                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();
    //
    //        public static final String CONTENT_TYPE =
    //                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
    //        public static final String CONTENT_ITEM_TYPE =
    //                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
    //
    //        // Table name
    //        public static final String TABLE_NAME = "categories";
    //
    //        public static final String COLUMN_CATEGORY = "category";
    //        public static final String COLUMN_POST_ID = "post_id";
    //
    //        public static Uri buildCategoryUri(long id) {
    //            return ContentUris.withAppendedId(CONTENT_URI, id);
    //        }
    //    }
}
