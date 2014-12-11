package gr.tsagi.jekyllforandroid.app.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import gr.tsagi.jekyllforandroid.app.Config;
import gr.tsagi.jekyllforandroid.app.data.PostsContract.*;
import gr.tsagi.jekyllforandroid.app.data.PostsDatabase.*;
import gr.tsagi.jekyllforandroid.app.utils.SelectionBuilder;

import static gr.tsagi.jekyllforandroid.app.utils.LogUtils.LOGV;
import static gr.tsagi.jekyllforandroid.app.utils.LogUtils.makeLogTag;


/**
 * Created by tsagi on 8/8/14.
 */
public class PostsProvider extends ContentProvider {

    private static final String TAG = makeLogTag(PostsProvider.class);


    private PostsDatabase mOpenHelper;
    // THe URI Matcher is used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int TAGS = 100;
    private static final int TAGS_ID = 101;

    private static final int CATEGORIES = 200;
    private static final int CATEGORIES_ID = 201;

    private static final int POSTS = 300;
    private static final int POSTS_SEARCH = 301;
    private static final int POSTS_ID = 302;
    private static final int POSTS_ID_TAGS = 303;
    private static final int POSTS_ID_CATEGORIES = 304;
    private static final int POSTS_DRAFTS = 305;
    private static final int POSTS_PUBLISHED = 306;

    private static final int PUBLISHED = 400;

    private static final int DRAFTS = 500;

    private static final int SEARCH_SUGGEST = 600;
    private static final int SEARCH_INDEX = 601;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static final SQLiteQueryBuilder sParametersQueryBuilder;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PostsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "tags", TAGS);
        matcher.addURI(authority, "tags/*", TAGS_ID);

        matcher.addURI(authority, "categories", CATEGORIES);
        matcher.addURI(authority, "categories/*", CATEGORIES_ID);

        matcher.addURI(authority, "posts", POSTS);
        matcher.addURI(authority, "posts/search/*", POSTS_SEARCH);
        matcher.addURI(authority, "posts/drafts/*", POSTS_DRAFTS);
        matcher.addURI(authority, "posts/published/*", POSTS_PUBLISHED);
        matcher.addURI(authority, "posts/*", POSTS_ID);
        matcher.addURI(authority, "posts/*/categories", POSTS_ID_CATEGORIES);
        matcher.addURI(authority, "posts/*/tags", POSTS_ID_TAGS);

        matcher.addURI(authority, "published", PUBLISHED);
        matcher.addURI(authority, "drafts", DRAFTS);

        matcher.addURI(authority, "search_suggest_query", SEARCH_SUGGEST);
        matcher.addURI(authority, "search_index", SEARCH_INDEX); // 'update' only

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PostsDatabase(getContext());
        return true;
    }

    private void deleteDatabase() {
        // TODO: wait for content provider operations to finish, then tear down
        mOpenHelper.close();
        Context context = getContext();
        PostsDatabase.deleteDatabase(context);
        mOpenHelper = new PostsDatabase(getContext());
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TAGS:
                return Tags.CONTENT_TYPE;
            case TAGS_ID:
                return Tags.CONTENT_ITEM_TYPE;
            case CATEGORIES:
                return Categories.CONTENT_TYPE;
            case CATEGORIES_ID:
                return Categories.CONTENT_TYPE;
            case POSTS:
                return Posts.CONTENT_TYPE;
            case POSTS_SEARCH:
                return Posts.CONTENT_TYPE;
            case POSTS_PUBLISHED:
                return Posts.CONTENT_TYPE;
            case POSTS_DRAFTS:
                return Posts.CONTENT_TYPE;
            case POSTS_ID:
                return Posts.CONTENT_ITEM_TYPE;
            case POSTS_ID_CATEGORIES:
                return Posts.CONTENT_TYPE;
            case POSTS_ID_TAGS:
                return Tags.CONTENT_TYPE;
            case PUBLISHED:
                return Published.CONTENT_TYPE;
            case DRAFTS:
                return Drafts.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** Returns a tuple of question marks. For example, if count is 3, returns "(?,?,?)". */
    private String makeQuestionMarkTuple(int count) {
        if (count < 1) {
            return "()";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(?");
        for (int i = 1; i < count; i++) {
            stringBuilder.append(",?");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    /** Adds the tags filter query parameter to the given builder. */
    private void addTagsFilter(SelectionBuilder builder, String tagsFilter) {
        // Note: for context, remember that post queries are done on a join of posts
        // and the posts_tags relationship table, and are GROUP'ed BY the post ID.
        String[] requiredTags = tagsFilter.split(",");
        if (requiredTags.length == 0) {
            // filtering by 0 tags -- no-op
            return;
        } else if (requiredTags.length == 1) {
            // filtering by only one tag, so a simple WHERE clause suffices
            builder.where(Tags.TAG_ID + "=?", requiredTags[0]);
        } else {
            // Filtering by multiple tags, so we must add a WHERE clause with an IN operator,
            // and add a HAVING statement to exclude groups that fall short of the number
            // of required tags. For example, if requiredTags is { "X", "Y", "Z" }, and a certain
            // post only has tags "X" and "Y", it will be excluded by the HAVING statement.
            String questionMarkTuple = makeQuestionMarkTuple(requiredTags.length);
            builder.where(Tags.TAG_ID + " IN " + questionMarkTuple, requiredTags);
            builder.having("COUNT(" + Qualified.POSTS_POST_ID + ") >= " + requiredTags.length);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String tagsFilter = uri.getQueryParameter(Posts.QUERY_PARAMETER_TAG_FILTER);
        final int match = sUriMatcher.match(uri);

        // avoid the expensive string concatenation below if not loggable
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            LOGV(TAG, "uri=" + uri + " match=" + match + " proj=" + Arrays.toString(projection) +
                    " selection=" + selection + " args=" + Arrays.toString(selectionArgs) + ")");
        }


        switch (match) {
            default: {
                // Most cases are handled with simple SelectionBuilder
                final SelectionBuilder builder = buildExpandedSelection(uri, match);

                // If a special filter was specified, try to apply it
                if (!TextUtils.isEmpty(tagsFilter)) {
                    addTagsFilter(builder, tagsFilter);
                }

                boolean distinct = !TextUtils.isEmpty(
                        uri.getQueryParameter(PostsContract.QUERY_PARAMETER_DISTINCT));

                Cursor cursor = builder
                        .where(selection, selectionArgs)
                        .query(db, distinct, projection, sortOrder, null);
                Context context = getContext();
                if (null != context) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                return cursor;
            }

        }
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        boolean syncToNetwork = !PostsContract.hasCallerIsSyncAdapterParameter(uri);
        switch (match) {
            case TAGS: {
                db.insertOrThrow(Tables.TAGS, null, values);
                notifyChange(uri);
                return PostsContract.Tags.buildTagUri(values.getAsString(Tags.TAG_ID));
            }
            case CATEGORIES: {
                db.insertOrThrow(Tables.CATEGORIES, null, values);
                notifyChange(uri);
                return Categories.buildCategoryUri(values.getAsString(Categories.CATEGORY_ID));
            }
            case POSTS: {
                db.insertOrThrow(Tables.POSTS, null, values);
                notifyChange(uri);
                return Posts.buildPostUri(values.getAsString(Posts.POST_ID));
            }
            case POSTS_ID_CATEGORIES: {
                db.insertOrThrow(Tables.POSTS_CATEGORIES, null, values);
                notifyChange(uri);
                return Categories.buildCategoryUri(values.getAsString(PostsCategories.CATEGORY_ID));
            }
            case POSTS_ID_TAGS: {
                db.insertOrThrow(Tables.POSTS_TAGS, null, values);
                notifyChange(uri);
                return Tags.buildTagUri(values.getAsString(Tags.TAG_ID));
            }
            case PUBLISHED: {
                db.insertOrThrow(Tables.PUBLISHED, null, values);
                notifyChange(uri);
                return Posts.buildPostUri(values.getAsString(
                        PublishedColumns.POST_ID));
            }
            case DRAFTS: {
                db.insertOrThrow(Tables.DRAFTS, null, values);
                notifyChange(uri);
                return Posts.buildPostUri(values.getAsString(
                        DraftsColumns.POST_ID));
            }
            case SEARCH_SUGGEST: {
                db.insertOrThrow(Tables.SEARCH_SUGGEST, null, values);
                notifyChange(uri);
                return SearchSuggest.CONTENT_URI;
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        if (match == SEARCH_INDEX) {
            // update the search index
            PostsDatabase.updatePostSearchIndex(db);
            return 1;
        }

        final SelectionBuilder builder = buildSimpleSelection(uri);

        int retVal = builder.where(selection, selectionArgs).update(db, values);
        notifyChange(uri);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uri == PostsContract.BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            deleteDatabase();
            notifyChange(uri);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        final int match = sUriMatcher.match(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    private void notifyChange(Uri uri) {
        // We only notify changes if the caller is not the sync adapter.
        // The sync adapter has the responsibility of notifying changes (it can do so
        // more intelligently than we can -- for example, doing it only once at the end
        // of the sync instead of issuing thousands of notifications for each record).
        if (!PostsContract.hasCallerIsSyncAdapterParameter(uri)) {
            Context context = getContext();
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TAGS: {
                return builder.table(Tables.TAGS);
            }
            case TAGS_ID: {
                final String tagId = Tags.getTagId(uri);
                return builder.table(Tables.TAGS)
                        .where(Tags.TAG_ID + "=?", tagId);
            }
            case CATEGORIES: {
                return builder.table(Tables.CATEGORIES);
            }
            case CATEGORIES_ID: {
                final String categoryId = Categories.getCategoryId(uri);
                return builder.table(Tables.CATEGORIES)
                        .where(Categories.CATEGORY_ID + "=?", categoryId);
            }
            case POSTS: {
                return builder.table(Tables.POSTS);
            }
            case POSTS_ID: {
                final String postId = Posts.getPostId(uri);
                return builder.table(Tables.POSTS)
                        .where(Posts.POST_ID + "=?", postId);
            }
            case POSTS_ID_TAGS: {
                final String postId = Posts.getPostId(uri);
                return builder.table(Tables.POSTS_TAGS)
                        .where(Posts.POST_ID + "=?", postId);
            }
            case POSTS_ID_CATEGORIES: {
                final String postId = Posts.getPostId(uri);
                return builder.table(Tables.POSTS_CATEGORIES)
                        .where(Posts.POST_ID + "=?", postId);
            }
            case POSTS_PUBLISHED: {
                final String postId = Posts.getPostId(uri);
                return builder.table(Tables.PUBLISHED)
                        .where(PostsContract.PublishedColumns.POST_ID + "=?", postId);
            }
            case POSTS_DRAFTS: {
                final String postId = Posts.getPostId(uri);
                return builder.table(Tables.DRAFTS)
                        .where(PostsContract.DraftsColumns.POST_ID + "=?", postId);
            }
            case PUBLISHED: {
                return builder.table(Tables.PUBLISHED);
            }
            case DRAFTS: {
                return builder.table(Tables.DRAFTS);
            }
            case SEARCH_SUGGEST: {
                return builder.table(Tables.SEARCH_SUGGEST);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case TAGS: {
                return builder.table(Tables.TAGS);
            }
            case TAGS_ID: {
                final String tagId = Tags.getTagId(uri);
                return builder.table(Tables.TAGS)
                        .where(Tags.TAG_ID + "=?", tagId);
            }
            case CATEGORIES: {
                return builder.table(Tables.CATEGORIES);
            }
            case CATEGORIES_ID: {
                final String categoryId = Categories.getCategoryId(uri);
                return builder.table(Tables.CATEGORIES)
                        .where(Categories.CATEGORY_ID + "=?", categoryId);
            }
            case POSTS: {
                // We query posts on the joined table of posts with rooms and tags.
                // Since there may be more than one tag per post, we GROUP BY post ID.
                // The starred posts ("my schedule") are associated with a user, so we
                // use the current user to select them properly
                return builder.table(Tables.POSTS_JOIN_CATEGORY_TAGS)
                        .mapToTable(Posts._ID, Tables.POSTS)
                        .mapToTable(Posts.POST_ID, Tables.POSTS)
                        .map(Posts.POST_PUBLISHED, "IFNULL(published, 0)")
                        .groupBy(Qualified.POSTS_POST_ID);
            }
            case POSTS_PUBLISHED: {
                return builder.table(Tables.POSTS_JOIN_PUBLISHED)
                        .mapToTable(Posts._ID, Tables.POSTS)
                        .mapToTable(Posts.POST_ID, Tables.POSTS)
                        .map(Posts.POST_PUBLISHED, "IFNULL(in_schedule, 0)")
                        .where(" " + Posts.POST_PUBLISHED + "=1")
                        .groupBy(Qualified.POSTS_POST_ID);
            }
            case POSTS_DRAFTS: {
                return builder.table(Tables.POSTS_JOIN_PUBLISHED)
                        .mapToTable(Posts._ID, Tables.POSTS)
                        .mapToTable(Posts.POST_ID, Tables.POSTS)
                        .map(Posts.POST_PUBLISHED, "IFNULL(in_schedule, 0)")
                        .where(Posts.POST_PUBLISHED + "=0")
                        .groupBy(Qualified.POSTS_POST_ID);
            }
            case POSTS_SEARCH: {
                final String query = Posts.getSearchQuery(uri);
                return builder.table(Tables.POSTS_JOIN_PUBLISHED)
                        .map(Posts.SEARCH_SNIPPET, Subquery.POSTS_SNIPPET)
                        .mapToTable(Posts._ID, Tables.POSTS)
                        .mapToTable(Posts.POST_ID, Tables.POSTS)
                        .map(Posts.POST_PUBLISHED, "IFNULL(in_schedule, 0)")
                        .where(PostsSearchColumns.BODY + " MATCH ?", query);
            }
            case POSTS_ID: {
                final String postId = Posts.getPostId(uri);
                return builder.table(Tables.POSTS_JOIN_PUBLISHED)
                        .mapToTable(Posts._ID, Tables.POSTS)
                        .mapToTable(Posts.POST_ID, Tables.POSTS)
                        .map(Posts.POST_PUBLISHED, "IFNULL(in_schedule, 0)")
                        .where(Qualified.POSTS_POST_ID + "=?", postId);
            }
            case POSTS_ID_TAGS: {
                final String postId = Posts.getPostId(uri);
                return builder.table(Tables.POSTS_TAGS_JOIN_TAGS)
                        .mapToTable(Tags._ID, Tables.TAGS)
                        .mapToTable(Tags.TAG_ID, Tables.TAGS)
                        .where(Qualified.POSTS_TAGS_POST_ID + "=?", postId);
            }
            case POSTS_ID_CATEGORIES: {
                final String postId = Posts.getPostId(uri);
                return builder.table(Tables.POSTS_CATEGORIES_JOIN_CATEGORIES)
                        .mapToTable(Categories._ID, Tables.CATEGORIES)
                        .mapToTable(Categories.CATEGORY_ID, Tables.CATEGORIES)
                        .where(Qualified.POSTS_CATEGORIES_POST_ID + "=?", postId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}
