package gr.tsagi.jekyllforandroid.app.provider;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;

import java.util.List;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.makeLogTag;

/**
 * Created by tsagi on 8/8/14.
 */

/**
 * Contract class for interacting with {@link PostsProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri}
 * are generated using stronger {@link String} identifiers, instead of
 * {@code int} {@link BaseColumns#_ID} values, which are prone to shuffle during
 * sync.
 */
public class PostsContract {

    private static final String TAG = makeLogTag(PostsContract.class);

    /**
     * Query parameter to create a distinct query.
     */
    public static final String QUERY_PARAMETER_DISTINCT = "distinct";
    public static final String OVERRIDE_ACCOUNTNAME_PARAMETER = "overrideAccount";

    public interface SyncColumns {
        /** Last time this entry was updated or synchronized. */
        String UPDATED = "updated";
    }

    interface TagsColumns {
        /** Unique string identifying this tag. For example, "TOPIC_ANDROID", "TYPE_CODELAB" */
        String TAG_ID = "tag_id";
        /** Tag name. For example, "Android". */
        String TAG_NAME = "tag_name";
    }

    interface CategoriesColumns {
        /** Unique string identifying this category. For example, "TOPIC_ANDROID", "TYPE_CODELAB" */
        String CATEGORY_ID = "tag_id";
        /** Category name. For example, "Android". */
        String CATEGORY_NAME = "tag_name";
    }

    interface PublishedColumns {
        String POST_ID = PostsColumns.POST_ID;
    }

    interface DraftsColumns {
        String POST_ID = PostsColumns.POST_ID;
    }

    interface PostsColumns {
        /** Unique string identifying this post. */
        String POST_ID = "post_id";
        /** Start time of this track. */
        String POST_DATE = "post_date";
        /** Title describing this track. */
        String POST_TITLE = "post_title";
        /** Body of text explaining this post in detail. */
        String POST_ABSTRACT = "post_abstract";
        /** The set of tags the post has. This is a comma-separated list of tags.*/
        String POST_TAGS = "post_tags";
        /** The set of categories the post has. This is a comma-separated list of categories.*/
        String POST_CATEGORIES = "post_categories";
        /** Flag indicating published status. */
        String POST_PUBLISHED = "post_published";
        /** The hashcode of the data used to create this record. */
        String POST_IMPORT_HASHCODE = "post_import_hashcode";
    }

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
    public static final String PATH_TAGS = "tags";
    public static final String PATH_CATEGORIES = "categories";
    public static final String PATH_PUBLISHED = "published";
    public static final String PATH_DRAFTS = "drafts";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";
    private static final String PATH_SEARCH_INDEX = "search_index";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_POSTS,
            PATH_TAGS,
            PATH_CATEGORIES,
            PATH_PUBLISHED,
            PATH_DRAFTS,
            PATH_SEARCH,
            PATH_SEARCH_SUGGEST,
            PATH_SEARCH_INDEX
    };

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Tags represent Post classifications. A post can have many tags. Tags can indicate,
     * for example, what a post pertains to (Android, Chrome, ...), what type
     * of post it is (post, codelab, office hours, ...) and what overall event theme
     * it falls under (Design, Develop, Distribute), amongst others.
     */
    public static class Tags implements TagsColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAGS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.jfa.tag";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.jfa.tag";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = TagsColumns.TAG_NAME + "ASC";

        /**
         * Build {@link Uri} that references all tags.
         */
        public static Uri buildTagsUri() {
            return CONTENT_URI;
        }

        /** Build a {@link Uri} that references a given tag. */
        public static Uri buildTagUri(String tagId) {
            return CONTENT_URI.buildUpon().appendPath(tagId).build();
        }

        /** Read {@link #TAG_ID} from {@link Tags} {@link Uri}. */
        public static String getTagId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Tags represent Post classifications. A post can have many tags. Tags can indicate,
     * for example, what a post pertains to (Android, Chrome, ...), what type
     * of post it is (post, codelab, office hours, ...) and what overall event theme
     * it falls under (Design, Develop, Distribute), amongst others.
     */
    public static class Categories implements CategoriesColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.jfa.category";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.jfa.category";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = CategoriesColumns.CATEGORY_NAME + "ASC";

        /**
         * Build {@link Uri} that references all categories.
         */
        public static Uri buildCategoriesUri() {
            return CONTENT_URI;
        }

        /** Build a {@link Uri} that references a given tag. */
        public static Uri buildCategoryUri(String categoryId) {
            return CONTENT_URI.buildUpon().appendPath(categoryId).build();
        }

        /** Read {@link #CATEGORY_ID} from {@link Categories} {@link Uri}. */
        public static String getCategoryId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Published represent the posts that the user has in his '_posts/' directory.
     * Each row of Published represents one post.
     */
    public static class Published implements PublishedColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PUBLISHED).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.jfa.published";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.jfa.published";

        /**
         * Build {@link Uri} that references all Published for the current user.
         */
        public static Uri buildPublishedUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_PUBLISHED).build();
        }

    }

    /**
     * Drafts represent the posts that the user has in his '_posts/' directory.
     * Each row of Drafts represents one post.
     */
    public static class Drafts implements DraftsColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DRAFTS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.jfa.drafts";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.jfa.drafts";

        /**
         * Build {@link Uri} that references all Drafts for the current user.
         */
        public static Uri buildDraftsUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_DRAFTS).build();
        }

    }

    /**
     * Each post has zero or more {@link Tags},
     * zero or more {@link Categories}.
     */
    public static class Posts implements PostsColumns, SyncColumns, BaseColumns {
        public static final String QUERY_PARAMETER_TAG_FILTER = "filter";
        public static final String QUERY_PARAMETER_CATEGORY_FILTER = "filter";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();
        public static final Uri CONTENT_PUBLISHED_URI =
                CONTENT_URI.buildUpon().appendPath(PATH_PUBLISHED).build();
        public static final Uri CONTENT_DRAFTS_URI =
                CONTENT_URI.buildUpon().appendPath(PATH_DRAFTS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.jfa.post";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.jfa.post";

        public static final String SEARCH_SNIPPET = "search_snippet";

        // ORDER BY clauses
        public static final String SORT_BY_DATE = POST_DATE + " ASC,"
                + POST_TITLE + " COLLATE NOCASE ASC";


        /** Build {@link Uri} for requested {@link #POST_ID}. */
        public static Uri buildPostUri(String postId) {
            return CONTENT_URI.buildUpon().appendPath(postId).build();
        }

        /**
         * Build {@link Uri} that references any {@link Categories} associated
         * with the requested {@link #POST_ID}.
         */
        public static Uri buildCategoriesDirUri(String postId) {
            return CONTENT_URI.buildUpon().appendPath(postId).appendPath(PATH_CATEGORIES).build();
        }

        /**
         * Build {@link Uri} that references any {@link Tags} associated with
         * the requested {@link #POST_ID}.
         */
        public static Uri buildTagsDirUri(String postId) {
            return CONTENT_URI.buildUpon().appendPath(postId).appendPath(PATH_TAGS).build();
        }

        /**
         * Build {@link Uri} that references posts that match the query. The query can be
         * multiple words separated with spaces.
         *
         * @param query The query. Can be multiple words separated by spaces.
         * @return {@link Uri} to the posts
         */
        public static Uri buildSearchUri(String query) {
            if (null == query) {
                query = "";
            }
            // convert "lorem ipsum dolor sit" to "lorem* ipsum* dolor* sit*"
            query = query.replaceAll(" +", " *") + "*";
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_SEARCH).appendPath(query).build();
        }

        public static boolean isSearchUri(Uri uri) {
            List<String> pathSegments = uri.getPathSegments();
            return pathSegments.size() >= 2 && PATH_SEARCH.equals(pathSegments.get(1));
        }

        public static boolean isDraftPostsInInterval(Uri uri) {
            return uri != null && uri.toString().startsWith(
                    CONTENT_URI.buildUpon().appendPath(PATH_DRAFTS).toString());
        }

        /** Read {@link #POST_ID} from {@link Posts} {@link Uri}. */
        public static String getPostId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getSearchQuery(Uri uri) {
            List<String> segments = uri.getPathSegments();
            if (2 < segments.size()) {
                return segments.get(2);
            }
            return null;
        }

        public static boolean hasTagFilterParam(Uri uri) {
            return uri != null && uri.getQueryParameter(QUERY_PARAMETER_TAG_FILTER) != null;
        }

        public static boolean hasCategoryFilterParam(Uri uri) {
            return uri != null && uri.getQueryParameter(QUERY_PARAMETER_CATEGORY_FILTER) != null;
        }

        /** Build {@link Uri} that references all posts that have ALL of the indicated tags. */
        public static Uri buildTagFilterUri(String[] requiredTags) {
            StringBuilder sb = new StringBuilder();
            for (String tag : requiredTags) {
                if (TextUtils.isEmpty(tag)) continue;
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(tag.trim());
            }
            if (sb.length() == 0) {
                // equivalent to "all posts"
                return CONTENT_URI;
            } else {
                // filter by the given set of tags
                return CONTENT_URI.buildUpon().appendQueryParameter(QUERY_PARAMETER_TAG_FILTER,
                        sb.toString()).build();
            }
        }

        /** Build {@link Uri} that references all posts that have ALL of the indicated tags. */
        public static Uri buildCategoryFilterUri(String[] requiredCategories) {
            StringBuilder sb = new StringBuilder();
            for (String category : requiredCategories) {
                if (TextUtils.isEmpty(category)) continue;
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(category.trim());
            }
            if (sb.length() == 0) {
                // equivalent to "all posts"
                return CONTENT_URI;
            } else {
                // filter by the given set of tags
                return CONTENT_URI.buildUpon().appendQueryParameter(QUERY_PARAMETER_CATEGORY_FILTER,
                        sb.toString()).build();
            }
        }
    }

    public static class SearchSuggest {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_SUGGEST).build();

        public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
                + " COLLATE NOCASE ASC";
    }

    public static class SearchIndex {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_INDEX).build();
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return TextUtils.equals("true",
                uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
    }

    /**
     * Adds an account override parameter to the URI.
     * The override parameter instructs the Content Provider to ignore the currently logged in
     * account and use the provided account when fetching account-specific data
     * (such as sessions in My Schedule).
     *
     */
    public static Uri addOverrideAccountName(Uri uri, String accountName) {
        return uri.buildUpon().appendQueryParameter(
                OVERRIDE_ACCOUNTNAME_PARAMETER, accountName).build();
    }

    public static String getOverrideAccountName(Uri uri) {
        return uri.getQueryParameter(OVERRIDE_ACCOUNTNAME_PARAMETER);
    }

    private PostsContract() {
    }

}
