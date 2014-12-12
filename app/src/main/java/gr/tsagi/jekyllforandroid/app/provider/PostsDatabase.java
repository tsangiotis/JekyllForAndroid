package gr.tsagi.jekyllforandroid.app.provider;

import android.app.SearchManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static gr.tsagi.jekyllforandroid.app.provider.PostsContract.*;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGD;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGW;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.makeLogTag;

/**
 * Created by tsagi on 8/8/14.
 */
public class PostsDatabase extends SQLiteOpenHelper {
    private static final String TAG = makeLogTag(PostsDatabase.class);

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 9;

    public static final String DATABASE_NAME = "posts.db";

    private final Context mContext;

    interface Tables {
        String TAGS = "tags";
        String CATEGORIES = "categories";
        String POSTS = "posts";
        String POSTS_TAGS = "posts_tags";
        String POSTS_CATEGORIES = "posts_categories";

        String PUBLISHED = "published";

        String POSTS_SEARCH = "posts_search";

        String SEARCH_SUGGEST = "search_suggest";

        String POSTS_JOIN_PUBLISHED = "posts "
                + "LEFT OUTER JOIN published ON posts.post_id=published.post_id ";

        String POSTS_JOIN_CATEGORY_TAGS = "posts "
                + "LEFT OUTER JOIN published ON posts.post_id=published.post_id "
                + "LEFT OUTER JOIN posts_tags ON posts.post_id=posts_tags.post_id"
                + "LEFT OUTER JOIN posts_categories ON posts.post_id=posts_categories.post_id";
    
        String POSTS_TAGS_JOIN_TAGS = "posts_tags "
                + "LEFT OUTER JOIN tags ON posts_tags.tag_id=tags.tag_id";

        String POSTS_CATEGORIES_JOIN_CATEGORIES = "posts_tags "
                + "LEFT OUTER JOIN categories ON posts_categories.category_id=categories" +
                ".category_id";

        // When tables get deprecated, add them to this list (so they get correctly deleted
        // on database upgrades)
        interface DeprecatedTables {
        };

    }

    private interface Triggers {
        // Deletes from dependent tables when corresponding posts are deleted.
        String POSTS_TAGS_DELETE = "posts_tags_delete";
        String POSTS_CATEGORIES_DELETE = "posts_categories_delete";
        String POSTS_PUBLISHED_DELETE = "posts_published_delete";

        // When triggers get deprecated, add them to this list (so they get correctly deleted
        // on database upgrades)
        interface DeprecatedTriggers {
        };
    }

    public interface PostsTags {
        String POST_ID = "post_id";
        String TAG_ID = "tag_id";
    }

    public interface PostsCategories {
        String POST_ID = "post_id";
        String CATEGORY_ID = "category_id";
    }

    interface PostsSearchColumns {
        String POST_ID = "post_id";
        String BODY = "body";
    }

    /** Fully-qualified field names. */
    private interface Qualified {
        String POSTS_SEARCH = Tables.POSTS_SEARCH + "(" + PostsSearchColumns.POST_ID
                + "," + PostsSearchColumns.BODY + ")";

        String POSTS_TAGS_POST_ID = Tables.POSTS_TAGS + "."
                + PostsTags.POST_ID;

        String POSTS_CATEGORIES_POST_ID = Tables.POSTS_CATEGORIES + "."
                + PostsCategories.POST_ID;

    }

    /** {@code REFERENCES} clauses. */
    private interface References {
        String TAG_ID = "REFERENCES " + Tables.TAGS + "(" + Tags.TAG_ID + ")";
        String CATEGORY_ID = "REFERENCES " + Tables.CATEGORIES + "(" + Categories.CATEGORY_ID + ")";
        String POST_ID = "REFERENCES " + Tables.POSTS + "(" + Posts.POST_ID + ")";
    }

    public PostsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TagsColumns.TAG_ID + " TEXT NOT NULL,"
                + TagsColumns.TAG_NAME + " TEXT NOT NULL,"
                + "UNIQUE (" + TagsColumns.TAG_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.CATEGORIES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CategoriesColumns.CATEGORY_ID + " TEXT NOT NULL,"
                + CategoriesColumns.CATEGORY_NAME + " TEXT NOT NULL,"
                + "UNIQUE (" + CategoriesColumns.CATEGORY_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.POSTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + PostsColumns.POST_ID + " TEXT NOT NULL,"
                + PostsColumns.POST_DATE + " TEXT NOT NULL,"
                + PostsColumns.POST_TITLE + " TEXT,"
                + PostsColumns.POST_ABSTRACT + " TEXT,"
                + PostsColumns.POST_TAGS + " TEXT,"
                + PostsColumns.POST_CATEGORIES + " TEXT,"
                + PostsColumns.POST_IMPORT_HASHCODE + " TEXT NOT NULL DEFAULT '',"
                + "UNIQUE (" + PostsColumns.POST_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.POSTS_TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PostsTags.POST_ID + " TEXT NOT NULL " + References.POST_ID + ","
                + PostsTags.TAG_ID + " TEXT NOT NULL " + References.TAG_ID + ","
                + "UNIQUE (" + PostsTags.POST_ID + ","
                + PostsTags.TAG_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.POSTS_CATEGORIES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PostsCategories.POST_ID + " TEXT NOT NULL " + References.POST_ID + ","
                + PostsCategories.CATEGORY_ID + " TEXT NOT NULL " + References.CATEGORY_ID + ","
                + "UNIQUE (" + PostsCategories.POST_ID + ","
                + PostsCategories.CATEGORY_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.PUBLISHED + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Published.POST_ID + " TEXT NOT NULL UNIQUE " + References.POST_ID);

        // Full-text search index. Update using updatePostSearchIndex method.
        // Use the porter tokenizer for simple stemming, so that "frustration" matches "frustrated."
        db.execSQL("CREATE VIRTUAL TABLE " + Tables.POSTS_SEARCH + " USING fts3("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PostsSearchColumns.BODY + " TEXT NOT NULL,"
                + PostsSearchColumns.POST_ID
                + " TEXT NOT NULL " + References.POST_ID + ","
                + "UNIQUE (" + PostsSearchColumns.POST_ID + ") ON CONFLICT REPLACE,"
                + "tokenize=porter)");

        // Search suggestions
        db.execSQL("CREATE TABLE " + Tables.SEARCH_SUGGEST + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT NOT NULL)");

        // Post deletion triggers
        db.execSQL("CREATE TRIGGER " + Triggers.POSTS_TAGS_DELETE + " AFTER DELETE ON "
                + Tables.POSTS + " BEGIN DELETE FROM " + Tables.POSTS_TAGS + " "
                + " WHERE " + Qualified.POSTS_TAGS_POST_ID + "=old." + Posts.POST_ID
                + ";" + " END;");

        db.execSQL("CREATE TRIGGER " + Triggers.POSTS_CATEGORIES_DELETE + " AFTER DELETE ON "
                + Tables.POSTS + " BEGIN DELETE FROM " + Tables.POSTS_CATEGORIES + " "
                + " WHERE " + Qualified.POSTS_CATEGORIES_POST_ID + "=old." + Posts.POST_ID
                + ";" + " END;");

        db.execSQL("CREATE TRIGGER " + Triggers.POSTS_PUBLISHED_DELETE + " AFTER DELETE ON "
                + Tables.POSTS + " BEGIN DELETE FROM " + Tables.PUBLISHED + " "
                + " WHERE " + Tables.PUBLISHED + "." + Published.POST_ID +
                "=old." + Posts.POST_ID
                + ";" + " END;");

    }

    /**
     * Updates the post search index. This should be done sparingly, as the queries are rather
     * complex.
     */
    static void updatePostSearchIndex(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + Tables.POSTS_SEARCH);

        db.execSQL("INSERT INTO " + Qualified.POSTS_SEARCH
                + " SELECT s." + Posts.POST_ID + ",("

                // Full text body
                + Posts.POST_TITLE + "||'; '||"
                + Posts.POST_ABSTRACT + "||'; '||"
                + "IFNULL(GROUP_CONCAT(t." + Speakers.SPEAKER_NAME + ",' '),'')||'; '||"
                + "'')"

                + " FROM " + Tables.POSTS + " s "
                + " LEFT OUTER JOIN"

                // Subquery resulting in post_id, speaker_id, speaker_name
                + "(SELECT " + Posts.POST_ID + "," + Qualified.SPEAKERS_SPEAKER_ID
                + "," + Speakers.SPEAKER_NAME
                + " FROM " + Tables.POSTS_SPEAKERS
                + " INNER JOIN " + Tables.SPEAKERS
                + " ON " + Qualified.POSTS_SPEAKERS_SPEAKER_ID + "="
                + Qualified.SPEAKERS_SPEAKER_ID
                + ") t"

                // Grand finale
                + " ON s." + Posts.POST_ID + "=t." + Posts.POST_ID
                + " GROUP BY s." + Posts.POST_ID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGD(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        //TODO: Cancel any sync currently in progress
//        Account account = AccountUtils.getActiveAccount(mContext);
//        if (account != null) {
//            LOGI(TAG, "Cancelling any pending syncs for account");
//            ContentResolver.cancelSync(account, PostsContract.CONTENT_AUTHORITY);
//        }

        // Current DB version. We update this variable as we perform upgrades to reflect
        // the current version we are in.
        int version = oldVersion;

        // Indicates whether the data we currently have should be invalidated as a
        // result of the db upgrade. Default is true (invalidate); if we detect that this
        // is a trivial DB upgrade, we set this to false.
        boolean dataInvalidated = true;

        LOGD(TAG, "After upgrade logic, at version " + version);

        // at this point, we ran out of upgrade logic, so if we are still at the wrong
        // version, we have no choice but to delete everything and create everything again.
        if (version != DATABASE_VERSION) {
            LOGW(TAG, "Upgrade unsuccessful -- destroying old data during upgrade");

            db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.POSTS_TAGS_DELETE);
            db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.POSTS_CATEGORIES_DELETE);
            db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.POSTS_PUBLISHED_DELETE);

            db.execSQL("DROP TABLE IF EXISTS " + Tables.TAGS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.POSTS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PUBLISHED);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.POSTS_TAGS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.POSTS_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.POSTS_SEARCH);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.SEARCH_SUGGEST);

            onCreate(db);
            version = DATABASE_VERSION;
        }

    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }


}
