package gr.tsagi.jekyllforandroid.app.jfa;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import gr.tsagi.jekyllforandroid.app.jfa.model.Category;
import gr.tsagi.jekyllforandroid.app.jfa.model.Post;
import gr.tsagi.jekyllforandroid.app.jfa.model.Tag;
import gr.tsagi.jekyllforandroid.app.provider.PostsContract;
import gr.tsagi.jekyllforandroid.app.provider.PostsDatabase;
import gr.tsagi.jekyllforandroid.app.util.TimeUtils;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGD;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGW;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.makeLogTag;

/**
 * Created by tsagi on 12/12/14.
 */
public class PostsHandler extends JSONHandler {
    private static final String TAG = makeLogTag(PostsHandler.class);
    private HashMap<String, Post> mPosts = new HashMap<String, Post>();
    private HashMap<String, Tag> mTagMap = null;
    private HashMap<String, Category> mCategoryMap = null;

    public PostsHandler(Context context) {
        super(context);
    }

    @Override
    public void process(JsonElement element) {
        for (Post post : new Gson().fromJson(element, Post[].class)) {
            mPosts.put(post.id, post);
        }
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = PostsContract.addCallerIsSyncAdapterParameter(
                PostsContract.Posts.CONTENT_URI);

        // build a map of post to post import hashcode so we know what to update,
        // what to insert, and what to delete
        HashMap<String, String> postHashCodes = loadPostHashCodes();
        boolean incrementalUpdate = (postHashCodes != null) && (postHashCodes.size() > 0);

        // set of posts that we want to keep after the sync
        HashSet<String> postsToKeep = new HashSet<String>();

        if (incrementalUpdate) {
            LOGD(TAG, "Doing incremental update for posts.");
        } else {
            LOGD(TAG, "Doing full (non-incremental) update for posts.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updatedPosts = 0;
        for (Post post : mPosts.values()) {

            // compute the incoming post's hashcode to figure out if we need to update
            String hashCode = post.getImportHashCode();
            postsToKeep.add(post.id);

            // add post, if necessary
            if (!incrementalUpdate || !postHashCodes.containsKey(post.id) ||
                    !postHashCodes.get(post.id).equals(hashCode)) {
                ++updatedPosts;
                boolean isNew = !incrementalUpdate || !postHashCodes.containsKey(post.id);
                buildPost(isNew, post, list);

                // add relationships to speakers and track
                buildCategoriesMapping(post, list);
                buildTagsMapping(post, list);
            }
        }

        int deletedPosts = 0;
        if (incrementalUpdate) {
            for (String postId : postHashCodes.keySet()) {
                if (!postsToKeep.contains(postId)) {
                    buildDeleteOperation(postId, list);
                    ++deletedPosts;
                }
            }
        }

        LOGD(TAG, "Posts: " + (incrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedPosts + " to update, " + deletedPosts + " to delete. New total: " +
                mPosts.size());
    }

    private void buildDeleteOperation(String postId, List<ContentProviderOperation> list) {
        Uri postUri = PostsContract.addCallerIsSyncAdapterParameter(
                PostsContract.Posts.buildPostUri(postId));
        list.add(ContentProviderOperation.newDelete(postUri).build());
    }

    private HashMap<String, String> loadPostHashCodes() {
        Uri uri = PostsContract.addCallerIsSyncAdapterParameter(
                PostsContract.Posts.CONTENT_URI);
        LOGD(TAG, "Loading post hashcodes for post import optimization.");
        Cursor cursor = mContext.getContentResolver().query(uri, PostHashcodeQuery.PROJECTION,
                null, null, null);
        if (cursor == null || cursor.getCount() < 1) {
            LOGW(TAG, "Warning: failed to load post hashcodes. Not optimizing post import.");
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        HashMap<String, String> hashcodeMap = new HashMap<String, String>();
        while (cursor.moveToNext()) {
            String postId = cursor.getString(PostHashcodeQuery.POST_ID);
            String hashcode = cursor.getString(PostHashcodeQuery.POST_IMPORT_HASHCODE);
            hashcodeMap.put(postId, hashcode == null ? "" : hashcode);
        }
        LOGD(TAG, "Post hashcodes loaded for " + hashcodeMap.size() + " posts.");
        cursor.close();
        return hashcodeMap;
    }

    StringBuilder mStringBuilder = new StringBuilder();

    private void buildPost(boolean isInsert,
                           Post post, ArrayList<ContentProviderOperation> list) {
        ContentProviderOperation.Builder builder;
        Uri allPostsUri = PostsContract
                .addCallerIsSyncAdapterParameter(PostsContract.Posts.CONTENT_URI);
        Uri thisPostUri = PostsContract
                .addCallerIsSyncAdapterParameter(PostsContract.Posts.buildPostUri(
                        post.id));

        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allPostsUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisPostUri);
        }

        builder.withValue(PostsContract.SyncColumns.UPDATED, System.currentTimeMillis())
                .withValue(PostsContract.Posts.POST_ID, post.id)
                .withValue(PostsContract.Posts.POST_TITLE, post.title)
                .withValue(PostsContract.Posts.POST_ABSTRACT, post.description)
                .withValue(PostsContract.Posts.POST_DATE, TimeUtils.timestampToMillis(post
                        .date, 0))
                .withValue(PostsContract.Posts.POST_TAGS, post.makeTagsList())
                        // Note: we store this comma-separated list of tags IN ADDITION
                        // to storing the tags in proper relational format (in the posts_tags
                        // relationship table). This is because when querying for posts,
                        // we don't want to incur the performance penalty of having to do a
                        // subquery for every record to figure out the list of tags of each post.
                .withValue(PostsContract.Posts.POST_CATEGORIES, post.makeCategoriesList())
                        // Note: we store this comma-separated list of tags IN ADDITION
                        // to storing the tags in proper relational format (in the posts_tags
                        // relationship table). This is because when querying for posts,
                        // we don't want to incur the performance penalty of having to do a
                        // subquery for every record to figure out the list of tags of each post.
                .withValue(PostsContract.Posts.POST_IMPORT_HASHCODE,
                        post.getImportHashCode());
        list.add(builder.build());
    }

    private void buildTagsMapping(Post post, ArrayList<ContentProviderOperation> list) {
        final Uri uri = PostsContract.addCallerIsSyncAdapterParameter(
                PostsContract.Posts.buildTagsDirUri(post.id));

        // delete any existing mappings
        list.add(ContentProviderOperation.newDelete(uri).build());

        // add a mapping (a post+tag tuple) for each tag in the post
        for (String tag : post.tags) {
            list.add(ContentProviderOperation.newInsert(uri)
                    .withValue(PostsDatabase.PostsTags.POST_ID, post.id)
                    .withValue(PostsDatabase.PostsTags.TAG_ID, tag).build());
        }
    }

    private void buildCategoriesMapping(Post post, ArrayList<ContentProviderOperation> list) {
        final Uri uri = PostsContract.addCallerIsSyncAdapterParameter(
                PostsContract.Posts.buildCategoriesDirUri(post.id));

        // delete any existing mappings
        list.add(ContentProviderOperation.newDelete(uri).build());

        // add a mapping (a post+tag tuple) for each tag in the post
        for (String category : post.categories) {
            list.add(ContentProviderOperation.newInsert(uri)
                    .withValue(PostsDatabase.PostsCategories.POST_ID, post.id)
                    .withValue(PostsDatabase.PostsCategories.CATEGORY_ID, category).build());
        }
    }

    public void setTagMap(HashMap<String, Tag> tagMap) {
        mTagMap = tagMap;
    }

    public void setCategoryMap(HashMap<String, Category> categoryMap) {
        mCategoryMap = categoryMap;
    }

    private interface PostHashcodeQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                PostsContract.Posts.POST_ID,
                PostsContract.Posts.POST_IMPORT_HASHCODE
        };
        int _ID = 0;
        int POST_ID = 1;
        int POST_IMPORT_HASHCODE = 2;
    };
}
