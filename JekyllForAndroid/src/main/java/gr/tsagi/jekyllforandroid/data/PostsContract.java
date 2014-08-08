package gr.tsagi.jekyllforandroid.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by tsagi on 8/8/14.
 */
public class PostsContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "gr.tsagi.jekyllforandroid";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://gr.tsagi.jekyllforandroid/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_POSTS = "posts";

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /* Inner class that defines the table contents of the location table */
    public static final class PostEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;

        // Table name
        public static final String TABLE_NAME = "posts";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATETEXT = "date";

        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_TAGS = "tags";

        public static final String COLUMN_CONTENT = "content";


        public static Uri buildPostUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
