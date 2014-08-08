package gr.tsagi.jekyllforandroid.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;

/**
 * Created by tsagi on 8/8/14.
 */
public class PostsDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "posts.db";

    public PostsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold posts. A post consists of the title, the date and the post type
        final String SQL_CREATE_POSTS_TABLE = "CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                PostEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                PostEntry.COLUMN_CATEGORY + " TEXT, " +
                PostEntry.COLUMN_TAGS + " TEXT, " +
                PostEntry.COLUMN_CONTENT + " TEXT);";

        sqLiteDatabase.execSQL(SQL_CREATE_POSTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
