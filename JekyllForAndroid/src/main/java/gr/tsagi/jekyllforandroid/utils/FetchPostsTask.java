package gr.tsagi.jekyllforandroid.utils;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;

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
    int type;

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
     * Take the List with the posts and parse the posts for data
     */
    private void getPostDataFromList(Repository repository, List<TreeEntry> postslist) {

        // Get and insert the new posts information into the database
        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(postslist.size());
        Log.d(LOG_TAG, "Number of posts: " + String.valueOf(postslist.size()));
        for (TreeEntry post : postslist) {

            String filename = post.getPath();
            String[] filenameParts = filename.split("\\.");
            String id = filenameParts[0];

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

            ContentValues postValues = new ParsePostData(mContext).getDataFromContent(id,
                    blobBytes, type);
            contentValuesVector.add(postValues);

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

        String path;
        String param = params[0];

        if (param.matches("p")) {
            type = 0;
            path = "_posts";
        } else {
            type = 1;
            path = "_drafts";
        }


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
            // TODO: No sync when the same sha. (Utility class ready for this!)
            final String treeSha = commitService.getCommit(repository, baseCommitSha).getSha();

            // TODO: Refactor naming here.
            List<TreeEntry> list = dataService.getTree(repository, treeSha).getTree();
            String dPos = null;
            for (TreeEntry aList : list) {
                if (aList.getPath().equals(path))
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
