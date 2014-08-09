package gr.tsagi.jekyllforandroid.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.TagsRelationsEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.TagEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.CategoryEntry;

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

        final String token = Utility.getToken(mContext);

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
    private void addTags(String[] tags, String title) {

        long id;

        for (String tag : tags) {
            // First, check if the location with this city name exists in the db
            Cursor cursor = mContext.getContentResolver().query(
                    TagEntry.CONTENT_URI,
                    new String[]{TagEntry._ID},
                    TagEntry.COLUMN_NAME + " = ?",
                    new String[]{tag},
                    null);

            // If yes get id
            if (cursor.moveToFirst()) {
                int locationIdIndex = cursor.getColumnIndex(TagEntry._ID);
                id = cursor.getLong(locationIdIndex);
            } else {    // If not create tag and get id
                ContentValues tagValues = new ContentValues();
                tagValues.put(TagEntry.COLUMN_NAME, tag);

                Uri locationInsertUri = mContext.getContentResolver()
                        .insert(TagEntry.CONTENT_URI, tagValues);

                id = ContentUris.parseId(locationInsertUri);
            }

            // We are going to bulkInsert the posts so we don't have yet ID's to assign.
            // Instead we use post titles.
            // TODO: Find a better way.
            ContentValues tagRelationValues = new ContentValues();
            tagRelationValues.put(TagsRelationsEntry.COLUMN_TAG_KEY, id);
            tagRelationValues.put(TagsRelationsEntry.COLUMN_POST_TITLE, title);

            mContext.getContentResolver().insert(TagsRelationsEntry.CONTENT_URI, tagRelationValues);

        }

    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param category The category name.
     * @return the row ID of the added location.
     */
    private long addCategory(String category) {

        // First, check if the location with this city name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                new String[]{CategoryEntry._ID},
                CategoryEntry.COLUMN_NAME + " = ?",
                new String[]{category},
                null);

        if (cursor.moveToFirst()) {
            int locationIdIndex = cursor.getColumnIndex(CategoryEntry._ID);
            return cursor.getLong(locationIdIndex);
        } else {
            ContentValues categoryValues = new ContentValues();
            categoryValues.put(CategoryEntry.COLUMN_NAME, category);

            Uri categoryInsertUri = mContext.getContentResolver()
                    .insert(CategoryEntry.CONTENT_URI, categoryValues);

            return ContentUris.parseId(categoryInsertUri);
        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getPostDataFromList(Repository repository, List<TreeEntry> postslist) {

        // Each post has these
        final String JK_TITLE = "title";
        final String JK_CATEGORY = "category";
        final String JK_TAGS = "tags";

        // Get and insert the new posts information into the database
        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(postslist.size());

        for (TreeEntry post : postslist) {

            long date;
            String title;
            String[] tags;
            String category;
            String content;

            String filename = post.getPath();
            Log.d("TreeSub", filename);
            String postSha = post.getSha();
            Blob postBlob = null;
            try {
                postBlob = dataService.getBlob(repository, postSha);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert postBlob != null;
            String postContent = postBlob.getContent();

            String[] lines = postContent.split(System.getProperty("line.separator"));
            StringBuilder stringBuilder = new StringBuilder();

            int yaml_dash = 0;
            String yamlStr = null;
            for (String line : lines) {
                if (line.equals("---")) {
                    yaml_dash++;
                }
                if (yaml_dash != 2)
                    if (!line.equals("---"))
                        yamlStr = yamlStr + line + "\n";

                if (yaml_dash == 2) {
                    if (!line.equals("---"))
                        if (line.equals(""))
                            stringBuilder.append("\n");
                        else
                            stringBuilder.append(line);
                }
            }

            content = stringBuilder.toString().replaceAll("\n", "\n\n");

            Yaml yaml = new Yaml();

            HashMap<String, String[]> map = (HashMap<String, String[]>) yaml.load(yamlStr);

            title = String.valueOf(map.get(JK_TITLE));
            tags = map.get(JK_TAGS);
            category = String.valueOf(map.get(JK_CATEGORY));

            // Insert the location into the database.
            long locationID = addCategory(category);

            int i = filename.indexOf('-', 1 + filename.indexOf('-', 1 + filename.indexOf('-')));
            date = Long.parseLong(filename.substring(0, i).replace("-", ""));

            addTags(tags, title);

            ContentValues postValues = new ContentValues();

            postValues.put(PostEntry.COLUMN_TITLE, title);
            postValues.put(PostEntry.COLUMN_DATETEXT, date);
            postValues.put(PostEntry.COLUMN_DRAFT, 0);  // What we add here is not a draft
            postValues.put(PostEntry.COLUMN_CONTENT, content);

            contentValuesVector.add(postValues);
        }

        if (contentValuesVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(PostEntry.CONTENT_URI, cvArray);
        }

    }

    @Override
    protected Void doInBackground(String... params) {

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
