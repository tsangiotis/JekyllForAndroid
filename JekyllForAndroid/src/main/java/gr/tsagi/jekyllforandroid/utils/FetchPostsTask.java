package gr.tsagi.jekyllforandroid.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import gr.tsagi.jekyllforandroid.data.PostsContract.CategoryEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.TagEntry;

/**
 * Created by tsagi on 1/30/14.
 */

public class FetchPostsTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchPostsTask.class.getSimpleName();
    private final Context mContext;

    // Create the needed services
    RepositoryService repositoryService;
    CommitService commitService;
    DataService dataService;

    public FetchPostsTask(Context context) {

        mContext = context;

        final String token = Utility.getToken(context);

        // Start the client
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);

        // Initiate services
        repositoryService = new RepositoryService();
        commitService = new CommitService(client);
        dataService = new DataService(client);
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param tags the String array of tags for this post.
     */
    private void addTags(String tags, String id) {

        Log.d(LOG_TAG, "All Tags: " + tags);
        String[] taglist = tags.replace(" ", "").split(",");

        Vector<ContentValues> tagValuesVector = new Vector<ContentValues>(taglist.length);

        ContentValues tagValues = new ContentValues();

        for (String tag : taglist) {
            Log.d(LOG_TAG, "Tags for: " + tag);

            // First, check if the tag name exists in the db
            Cursor cursor = mContext.getContentResolver().query(
                    TagEntry.CONTENT_URI,
                    new String[]{TagEntry.COLUMN_POST_ID, TagEntry.COLUMN_TAG},
                    TagEntry.COLUMN_POST_ID + " = ? AND " + TagEntry.COLUMN_TAG,
                    null,
                    null);

            // If yes, see if it is assigned to post.
            if (!cursor.moveToFirst()) {
                cursor.close();
                tagValues.put(TagEntry.COLUMN_POST_ID, id);
                tagValues.put(TagEntry.COLUMN_TAG, tag);
                tagValuesVector.add(tagValues);

            }
        }

        if (tagValuesVector.size() > 0) {
            ContentValues[] tArray = new ContentValues[tagValuesVector.size()];
            tagValuesVector.toArray(tArray);
            mContext.getContentResolver().bulkInsert(TagEntry.CONTENT_URI, tArray);
            Log.d(LOG_TAG, "Inserted Tag Values.");
        } else {
            Log.d(LOG_TAG, "No Tag Values to insert.");
        }

    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param category The category name.
     * @return the row ID of the added location.
     */
    private void addCategory(String category, String id) {

        ContentValues categoryValues = new ContentValues();

        // First, check if the tag name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                new String[]{CategoryEntry.COLUMN_POST_ID, CategoryEntry.COLUMN_CATEGORY},
                CategoryEntry.COLUMN_POST_ID + " = ? AND " + CategoryEntry.COLUMN_CATEGORY,
                null,
                null);

        // If yes, see if it is assigned to post.
        if (!cursor.moveToFirst()) {
            cursor.close();
            categoryValues.put(CategoryEntry.COLUMN_POST_ID, id);
            categoryValues.put(CategoryEntry.COLUMN_CATEGORY, category);

            mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, categoryValues);
            Log.d(LOG_TAG, "Inserted Tag Values.");

        }

        Log.d(LOG_TAG, "No Tag Values to insert.");

    }

    /**
     * Take the List with the posts and parse the posts for data
     */
    private void getPostDataFromList(Repository repository, List<TreeEntry> postslist) {

        // Each post has these
        final String JK_TITLE = "title";
        final String JK_CATEGORY = "category";
        final String JK_TAGS = "tags";

        // Get and insert the new posts information into the database
        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(postslist.size());
        Log.d(LOG_TAG, "Number of posts: " + String.valueOf(postslist.size()));
        for (TreeEntry post : postslist) {

            String filename = post.getPath();
            String[] filenameParts = filename.split("\\.");
            String id = filenameParts[0];

            long date;
            String title;
            String tags;
            String category;
            String content;

            Log.d(LOG_TAG, "TreeSub: " + id);
            String postSha = post.getSha();
            Blob postBlob = null;
            try {
                postBlob = dataService.getBlob(repository, postSha).setEncoding(Blob.ENCODING_UTF8);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert postBlob != null;
            String blobBytes = postBlob.getContent();



            String postContent = null;

            // Blobs return with Base64 encoding so we have to UTF-8 them.
            byte[] bytes = Base64.decode(blobBytes, Base64.DEFAULT);
            try {
                postContent = new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            StringBuilder stringBuilder = new StringBuilder();

            InputStream is;
            BufferedReader r;

            is = new ByteArrayInputStream(postContent.getBytes());
            // read it with BufferedReader
            r = new BufferedReader(new InputStreamReader(is));
            String line;

            int yaml_dash = 0;
            String yamlStr = null;
            try {
                while ((line = r.readLine()) != null) {
                    if (line.equals("---")) {
                        yaml_dash++;
                    }
                    if (yaml_dash < 2) {
                        if (!line.equals("---"))
                            yamlStr = yamlStr + line + "\n";
                    }
                    if (yaml_dash >= 2) {
                        if (!line.equals("---"))
                            if (line.equals(""))
                                stringBuilder.append("\n");
                            else
                                stringBuilder.append(line);
                    }
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            content = stringBuilder.toString();

            Yaml yaml = new Yaml();

            HashMap<String, String[]> map = (HashMap<String, String[]>) yaml.load(yamlStr);
            HashMap<String, String> postmap = new HashMap<String, String>();

            postmap.put("title", String.valueOf(map.get("title")));
            postmap.put("category", String.valueOf(map.get("category")));
            postmap.put("tags", String.valueOf(map.get("tags")).replace("[", "").replace("]", ""));
            postmap.put("content", content);


            title = postmap.get(JK_TITLE);
            tags = postmap.get(JK_TAGS);
            category = postmap.get(JK_CATEGORY);

            Log.d(LOG_TAG, "Title: " + title);
            Log.d(LOG_TAG, "Tags: " + tags);
            Log.d(LOG_TAG, "Category: " + category);

            int i = id.indexOf('-', 1 + id.indexOf('-', 1 + id.indexOf('-')));
            date = Long.parseLong(id.substring(0, i).replace("-", ""));

            addTags(tags, id);
            addCategory(category, id);

            // First, check if the location with this city name exists in the db
            Cursor cursorId = mContext.getContentResolver().query(
                    PostEntry.CONTENT_URI,
                    new String[]{PostEntry.COLUMN_POST_ID},
                    PostEntry.COLUMN_POST_ID + " = ?",
                    new String[]{id},
                    null);

            Cursor cursorContent = mContext.getContentResolver().query(
                    PostEntry.CONTENT_URI,
                    new String[]{PostEntry.COLUMN_CONTENT},
                    PostEntry.COLUMN_CONTENT + " = ?",
                    new String[]{content},
                    null);

            ContentValues postValues = new ContentValues();

            if (cursorId.moveToFirst()) {
                cursorId.close();
                if (!cursorContent.moveToFirst()) {
                    cursorContent.close();
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(PostEntry.COLUMN_CONTENT, content);
                    if (updateValues.size() > 0) {
                        mContext.getContentResolver().update(PostEntry.CONTENT_URI, updateValues,
                                PostEntry.COLUMN_POST_ID + " = \"" + id + "\"", null);
                        Log.d(LOG_TAG, "Updated Value.");
                    } else {
                        Log.d(LOG_TAG, "No Values to insert.");
                    }

                }
            } else {
                cursorId.close();
                cursorContent.close();

                postValues.put(PostEntry.COLUMN_TITLE, title);
                postValues.put(PostEntry.COLUMN_DATETEXT, date);
                postValues.put(PostEntry.COLUMN_DRAFT, 0);  // What we add here is not a draft
                postValues.put(PostEntry.COLUMN_CONTENT, content);
                postValues.put(PostEntry.COLUMN_POST_ID, id);

                contentValuesVector.add(postValues);

            }
        }

        if (contentValuesVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(PostEntry.CONTENT_URI, cvArray);
            Log.d(LOG_TAG, "Inserted Values.");
        } else {
            Log.d(LOG_TAG, "No Values to insert.");
        }

    }

    @Override
    protected Void doInBackground(String... params) {

        Log.d(LOG_TAG, "Background started");

        // TODO: Support subdirectories
        final String user = Utility.getUser(mContext);
        final String repo = Utility.getRepo(mContext);

        // get some sha's from current state in git
        Log.d(LOG_TAG, user + " - " + repo);
        Repository repository;

        try {
            repository = repositoryService.getRepository(user, repo);
            final String baseCommitSha = repositoryService.getBranches(repository).get(0)
                    .getCommit()
                    .getSha();
            final String treeSha = commitService.getCommit(repository, baseCommitSha).getSha();

            // TODO: Refactor naming here.
            List<TreeEntry> list = dataService.getTree(repository, treeSha).getTree();
            String dPos = null;
            for (TreeEntry aList : list) {
                if (aList.getPath().equals("_posts"))
                    dPos = aList.getSha();
            }

            List<TreeEntry> postslist = dataService.getTree(repository, dPos).getTree();
            getPostDataFromList(repository, postslist);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
