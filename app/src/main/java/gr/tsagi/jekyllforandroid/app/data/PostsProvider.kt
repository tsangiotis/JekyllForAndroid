package gr.tsagi.jekyllforandroid.app.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import gr.tsagi.jekyllforandroid.app.data.PostsContract.PostEntry


/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 8/8/14
\* Time: 19:48
\*/
class PostsProvider : ContentProvider() {
    private var mOpenHelper: PostsDbHelper? = null

    private fun getPostWithCategoryAndTags(uri: Uri, projection: Array<String>, sortOrder: String): Cursor {
        val id = PostEntry.getIdFromUri(uri)

        val selectionArgs: Array<String>
        val selection: String

        selection = sPostSelection
        selectionArgs = arrayOf(id)

        return sParametersQueryBuilder.query(mOpenHelper!!.readableDatabase,
                projection,
                selection,
                selectionArgs, null, null,
                sortOrder
        )
    }

    private fun sPostsByStatus(uri: Uri, projection: Array<String>, sortOrder: String): Cursor {
        var status = PostEntry.getStatusFromUri(uri)

        if (status == "published")
            status = "0"
        else
            status = "1"

        val selectionArgs: Array<String>
        val selection: String

        selection = sPostStatusSelection
        selectionArgs = arrayOf(status)

        return mOpenHelper!!.readableDatabase.query(
                PostEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs, null, null,
                sortOrder
        )
    }

    override fun onCreate(): Boolean {
        mOpenHelper = PostsDbHelper(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
                       sortOrder: String?): Cursor? {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        var retCursor: Cursor? = null
        when (sUriMatcher.match(uri)) {
        // "posts/*"
            POST_STATUS -> {
                retCursor = sPostsByStatus(uri, projection!!, sortOrder!!)
            }
        // "posts/*/*"
            POST_ID -> {
                retCursor = getPostWithCategoryAndTags(uri, projection!!, sortOrder!!)
            }//                dumpCursor(retCursor);
        // "post"
            POST -> {
                retCursor = mOpenHelper!!.readableDatabase.query(
                        PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs, null, null,
                        sortOrder
                )
            }
        // "tag"
            TAG -> {
            }//                retCursor = mOpenHelper.getReadableDatabase().query(
        //                        TagEntry.TABLE_NAME,
        //                        projection,
        //                        selection,
        //                        selectionArgs,
        //                        null,
        //                        null,
        //                        sortOrder
        //                );
        // "category"
            CATEGORY -> {
            }//                retCursor = mOpenHelper.getReadableDatabase().query(
        //                        CategoryEntry.TABLE_NAME,
        //                        projection,
        //                        selection,
        //                        selectionArgs,
        //                        null,
        //                        null,
        //                        sortOrder
        //                );
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
        //        dumpCursor(retCursor);
        retCursor!!.setNotificationUri(context!!.contentResolver, uri)
        return retCursor
    }

    private fun dumpCursor(myCursor: Cursor?) {
        if (myCursor == null) {
            Log.w(LOG_TAG, "Null cursor")
        } else {
            try {
                if (myCursor.moveToFirst()) {
                    val columns = myCursor.columnNames
                    val sbHeader = StringBuilder()
                    for (columnName in columns) {
                        sbHeader.append(columnName).append(", ")
                    }
                    Log.i(LOG_TAG, sbHeader.toString())
                    do {
                        val sbRow = StringBuilder()
                        for (columnName in columns) {
                            sbRow.append(myCursor.getString(myCursor.getColumnIndex(columnName))).append(", ")
                        }
                        Log.i(LOG_TAG, sbRow.toString())
                    } while (myCursor.moveToNext())
                } else {
                    Log.w(LOG_TAG, "Empty cursor")
                }
            } catch (ex: Exception) {
                Log.e(LOG_TAG, ex.toString())
            } finally {
                if (!myCursor.isClosed) {
                    myCursor.moveToFirst()
                }
            }
        }
    }

    override fun getType(uri: Uri): String? {
        // Use the Uri Matcher to determine what kind of URI this is.
        val match = sUriMatcher.match(uri)

        when (match) {
            POST_ID -> return PostEntry.CONTENT_TYPE
            POST -> return PostEntry.CONTENT_TYPE
            TAG,
                //                return TagEntry.CONTENT_TYPE;
            CATEGORY,
                //                return CategoryEntry.CONTENT_TYPE;
            else -> {
                throw UnsupportedOperationException("Unknown uri: " + uri)
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = mOpenHelper!!.writableDatabase
        val match = sUriMatcher.match(uri)
        var returnUri: Uri? = null

        when (match) {
            POST -> {
                val _id = db.insert(PostEntry.TABLE_NAME, null, values)
                if (_id > 0)
                    returnUri = PostEntry.buildPostUri(_id)
                else
                    throw android.database.SQLException("Failed to insert row into " + uri)
            }
            TAG -> {
            }//                long _id = db.insert(TagEntry.TABLE_NAME, null, values);
        //                if ( _id > 0 )
        //                    returnUri = TagEntry.buildTagUri(_id);
        //                else
        //                    throw new android.database.SQLException("Failed to insert row into " + uri);
            CATEGORY -> {
            }//                long _id = db.insert(CategoryEntry.TABLE_NAME, null, values);
        //                if ( _id > 0 )
        //                    returnUri = CategoryEntry.buildCategoryUri(_id);
        //                else
        //                    throw new android.database.SQLException("Failed to insert row into " + uri);
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
        context!!.contentResolver.notifyChange(uri, null)
        return returnUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = mOpenHelper!!.writableDatabase
        val match = sUriMatcher.match(uri)
        var rowsDeleted = 0
        when (match) {
            POST -> rowsDeleted = db.delete(
                    PostEntry.TABLE_NAME, selection, selectionArgs)
            TAG -> {
            }
            CATEGORY -> {
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }//                rowsDeleted = db.delete(
        //                        TagEntry.TABLE_NAME, selection, selectionArgs);
        //                rowsDeleted = db.delete(
        //                        CategoryEntry.TABLE_NAME, selection, selectionArgs);
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return rowsDeleted
    }


    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val db = mOpenHelper!!.writableDatabase
        val match = sUriMatcher.match(uri)
        var rowsUpdated = 0

        when (match) {
            POST -> rowsUpdated = db.update(PostEntry.TABLE_NAME, values, selection,
                    selectionArgs)
            TAG -> {
            }
            CATEGORY -> {
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }//                rowsUpdated = db.update(TagEntry.TABLE_NAME, values, selection,
        //                        selectionArgs);
        //                rowsUpdated = db.update(CategoryEntry.TABLE_NAME, values, selection,
        //                        selectionArgs);
        if (rowsUpdated != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return rowsUpdated
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val db = mOpenHelper!!.writableDatabase
        val match = sUriMatcher.match(uri)
        var returnCount: Int
        when (match) {
            POST -> {
                db.beginTransaction()
                returnCount = 0
                try {
                    for (value in values) {
                        val _id = db.insert(PostEntry.TABLE_NAME, null, value)
                        if (_id != -1L) {
                            returnCount++
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                context!!.contentResolver.notifyChange(uri, null)
                return returnCount
            }
            TAG -> {
                db.beginTransaction()
                returnCount = 0
                try {
                    for (value in values) {
                        //                        long _id = db.insert(TagEntry.TABLE_NAME, null, value);
                        //                        if (_id != -1) {
                        //                            returnCount++;
                        //                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                context!!.contentResolver.notifyChange(uri, null)
                return returnCount
            }
            CATEGORY -> {
                db.beginTransaction()
                returnCount = 0
                try {
                    for (value in values) {
                        //                        long _id = db.insert(CategoryEntry.TABLE_NAME, null, value);
                        //                        if (_id != -1) {
                        //                            returnCount++;
                        //                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                context!!.contentResolver.notifyChange(uri, null)
                return returnCount
            }
            else -> return super.bulkInsert(uri, values)
        }
    }

    companion object {

        private val LOG_TAG = PostsProvider::class.java.simpleName

        // THe URI Matcher is used by this content provider.
        private val sUriMatcher = buildUriMatcher()

        private val POST = 100
        private val POST_ID = 101
        private val POST_STATUS = 102
        private val CATEGORY = 200
        private val CATEGORY_PER_POST = 201
        private val TAG = 300
        private val TAG_PER_POST = 301

        private val sParametersQueryBuilder: SQLiteQueryBuilder

        // TODO: select tags.tagname as tagname from posts cross join tags where posts
        // .id=5;

        init {
            sParametersQueryBuilder = SQLiteQueryBuilder()
            sParametersQueryBuilder.tables = PostEntry.TABLE_NAME
        }

        private val sPostSelection =
                PostEntry.TABLE_NAME +
                        "." + PostEntry.COLUMN_POST_ID + " = ? "
        private val sPostStatusSelection =
                PostEntry.TABLE_NAME +
                        "." + PostEntry.COLUMN_DRAFT + " = ? "

        private fun buildUriMatcher(): UriMatcher {
            // I know what you're thinking.  Why create a UriMatcher when you can use regular
            // expressions instead?  Because you're not crazy, that's why.

            // All paths added to the UriMatcher have a corresponding code to return when a match is
            // found.  The code passed into the constructor represents the code to return for the root
            // URI.  It's common to use NO_MATCH as the code for this case.
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            val authority = PostsContract.CONTENT_AUTHORITY

            // For each type of URI you want to add, create a corresponding code.
            matcher.addURI(authority, PostsContract.PATH_POSTS, POST)
            matcher.addURI(authority, PostsContract.PATH_POSTS + "/*", POST_STATUS)
            matcher.addURI(authority, PostsContract.PATH_POSTS + "/*/*", POST_ID)

            matcher.addURI(authority, PostsContract.PATH_TAGS, TAG)

            matcher.addURI(authority, PostsContract.PATH_CATEGORIES, CATEGORY)
            matcher.addURI(authority, PostsContract.PATH_CATEGORIES + "/$", CATEGORY_PER_POST)

            return matcher
        }
    }
}
