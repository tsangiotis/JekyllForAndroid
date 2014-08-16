package gr.tsagi.jekyllforandroid.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import gr.tsagi.jekyllforandroid.data.PostsContract.CategoryEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.TagEntry;


/**
 * Created by tsagi on 8/8/14.
 */
public class PostsProvider extends ContentProvider {

    private static final String LOG_TAG = PostsProvider.class.getSimpleName();

    // THe URI Matcher is used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PostsDbHelper mOpenHelper;

    private static final int POST = 100;
    private static final int POST_ID = 101;
    private static final int CATEGORY = 200;
    private static final int CATEGORY_PER_POST = 201;
    private static final int TAG = 300;
    private static final int TAG_PER_POST = 301;

    private static final SQLiteQueryBuilder sParametersQueryBuilder;

    static{
        sParametersQueryBuilder = new SQLiteQueryBuilder();
        //ursor cur=db.rawQuery("SELECT f.* FROM food f INNER JOIN ingredient i ON f.id = i.food_id INNER JOIN meal m ON i.meal_id = 1 ",new String [] {});
        sParametersQueryBuilder.setTables(
                PostEntry.TABLE_NAME + " INNER JOIN " +
                        CategoryEntry.TABLE_NAME +
                        " ON " + PostEntry.TABLE_NAME +
                        "." + PostEntry.COLUMN_POST_ID +
                        " = " + CategoryEntry.TABLE_NAME +
                        "." + CategoryEntry.COLUMN_POST_ID +
                        " INNER JOIN " +
                        TagEntry.TABLE_NAME +
                        " ON " + PostEntry.TABLE_NAME +
                        "." + PostEntry.COLUMN_POST_ID +
                        " = " + TagEntry.TABLE_NAME +
                        "." + TagEntry.COLUMN_POST_ID
                );
    }

    private static final String sTagCategorySelection =
            TagEntry.TABLE_NAME+
                    "." + TagEntry.COLUMN_POST_ID + " = ? ";

    private static final String sPostCategorySelection =
            CategoryEntry.TABLE_NAME+
                    "." + CategoryEntry.COLUMN_POST_ID + " = ? ";

    private static final String sPostSelection =
            PostEntry.TABLE_NAME +
                    "." + PostEntry.COLUMN_POST_ID + " = ? ";

    private Cursor getPostWithCategoryAndTags(Uri uri, String[] projection, String sortOrder) {
        String id = PostEntry.getIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sPostSelection;
        selectionArgs = new String[]{id};

        return sParametersQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PostsContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, PostsContract.PATH_POSTS, POST);
        matcher.addURI(authority, PostsContract.PATH_POSTS + "/*", POST_ID);

        matcher.addURI(authority, PostsContract.PATH_TAGS, TAG);
        matcher.addURI(authority, PostsContract.PATH_TAGS + "/#", TAG_PER_POST);

        matcher.addURI(authority, PostsContract.PATH_CATEGORIES, CATEGORY);
        matcher.addURI(authority, PostsContract.PATH_CATEGORIES + "/$", CATEGORY_PER_POST);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PostsDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "posts/*"
            case POST_ID: {
                retCursor = getPostWithCategoryAndTags(uri, projection, sortOrder);
                dumpCursor(retCursor);
                break;
            }
            // "post"
            case POST: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "tag"
            case TAG: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TagEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "category"
            case CATEGORY: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private void dumpCursor(Cursor myCursor) {
        if (myCursor == null) {
            Log.w(LOG_TAG, "Null cursor");
        } else {
            try {
                if (myCursor.moveToFirst()) {
                    String [] columns = myCursor.getColumnNames();
                    StringBuilder sbHeader = new StringBuilder();
                    for (String columnName : columns) {
                        sbHeader.append(columnName).append(", ");
                    }
                    Log.i(LOG_TAG, sbHeader.toString());
                    do {
                        StringBuilder sbRow = new StringBuilder();
                        for (String columnName : columns) {
                            sbRow.append(myCursor.getString(myCursor.getColumnIndex(columnName))).append(", ");
                        }
                        Log.i(LOG_TAG, sbRow.toString());
                    } while (myCursor.moveToNext());
                } else {
                    Log.w(LOG_TAG, "Empty cursor");
                }
            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.toString());
            } finally {
                if (!myCursor.isClosed()) {
                    myCursor.moveToFirst();
                }
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case POST_ID:
                return PostEntry.CONTENT_TYPE;
            case POST:
                return PostEntry.CONTENT_TYPE;
            case TAG:
                return TagEntry.CONTENT_TYPE;
            case CATEGORY:
                return CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case POST: {
                long _id = db.insert(PostEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PostEntry.buildPostUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TAG: {
                long _id = db.insert(TagEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TagEntry.buildTagUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CATEGORY: {
                long _id = db.insert(CategoryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CategoryEntry.buildCategoryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case POST:
                rowsDeleted = db.delete(
                        PostEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TAG:
                rowsDeleted = db.delete(
                        TagEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsDeleted = db.delete(
                        CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case POST:
                rowsUpdated = db.update(PostEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TAG:
                rowsUpdated = db.update(TagEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CATEGORY:
                rowsUpdated = db.update(CategoryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case POST:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PostEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case TAG:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TagEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case CATEGORY:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(CategoryEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
