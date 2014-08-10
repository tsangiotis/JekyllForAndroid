package gr.tsagi.jekyllforandroid.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.TagsRelationsEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.TagEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.CategoryEntry;

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
                PostEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PostEntry.COLUMN_DRAFT + " INTEGER NOT NULL, " +
                PostEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                PostEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                PostEntry.COLUMN_CONTENT + " TEXT);";

        final String SQL_CREATE_TAGS_RELATIONS_TABLE = "CREATE TABLE " +
                TagsRelationsEntry.TABLE_NAME + " (" +
                TagsRelationsEntry.COLUMN_POST_TITLE + " TEXT NOT NULL, " +
                TagsRelationsEntry.COLUMN_TAG_KEY + " INTEGER NOT NULL);";

        final String SQL_CREATE_TAGS_TABLE = "CREATE TABLE " + TagEntry.TABLE_NAME + " (" +
                TagEntry._ID + " INTEGER PRIMARY KEY, " +
                TagEntry.COLUMN_NAME + " TEXT NOT NULL);";

        final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " + CategoryEntry.TABLE_NAME +
                " (" +
                CategoryEntry._ID + " INTEGER PRIMARY KEY, " +
                CategoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                CategoryEntry.COLUMN_POST_KEY + " INTEGER NOT NULL," +

                // Set up the post column as a foreign key to location table.
                " FOREIGN KEY (" + CategoryEntry.COLUMN_POST_KEY + ") REFERENCES " +
                PostEntry.TABLE_NAME + " (" + PostEntry._ID + ") );";

        sqLiteDatabase.execSQL(SQL_CREATE_POSTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TAGS_RELATIONS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TAGS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORIES_TABLE);

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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TagsRelationsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TagEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
