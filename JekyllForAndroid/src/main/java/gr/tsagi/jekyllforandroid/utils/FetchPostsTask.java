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

import gr.tsagi.jekyllforandroid.data.PostsContract;

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

    Utility utility;

    public FetchPostsTask(Context context) {

        mContext = context;

        utility = new Utility(mContext);

        final String token = utility.getToken();

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
    private void getPostDataFromList(Repository repository, List<TreeEntry> postslist, int type) {

        // Get and insert the new posts information into the database
        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(postslist.size());
        for (TreeEntry post : postslist) {

            if (post.getType().equals("blob")) {

                String filename = post.getPath();

                Log.d(LOG_TAG, filename);
                String[] filenameParts = filename.split("\\.");
                String id = filenameParts[0];

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
                if (postValues.size() > 0)
                    contentValuesVector.add(postValues);

                if (contentValuesVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[contentValuesVector.size()];
                    contentValuesVector.toArray(cvArray);
                    mContext.getContentResolver().bulkInsert(PostsContract.PostEntry.CONTENT_URI, cvArray);
                    Log.d(LOG_TAG, "Inserted Values.");
                } else {
                    Log.d(LOG_TAG, "No Values to insert.");
                }
            } else {
                try {
                    List<TreeEntry> subdir =  dataService.getTree(repository, post.getSha()).getTree();
                    getPostDataFromList(repository, subdir, type);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    protected Void doInBackground(String... params) {

        Log.d(LOG_TAG, "Background started");

        // TODO: Support subdirectories
        final String user = utility.getUser();
        final String repo = utility.getRepo();

        // get some sha's from current state in git
        Log.d(LOG_TAG, user + " - " + repo);
        Repository repository;

        try {
            repository = repositoryService.getRepository(user, repo);
            final String baseCommitSha = repositoryService.getBranches(repository).get(0)
                    .getCommit()
                    .getSha();
            // TODO: No sync when the same sha. (Utility class ready for this!)
            String oldSha = utility.getBaseCommitSha();

//            if (baseCommitSha.equals(oldSha)) {
//                Log.d(LOG_TAG, "No Sync");
//                this.cancel(true);
//                return null;
//            } else {
//                Log.d(LOG_TAG, "Syncing...");
//                utility.setBaseCommitSha(baseCommitSha);
//            }

            final String treeSha = commitService.getCommit(repository, baseCommitSha).getSha();

            // TODO: Refactor naming here.
            List<TreeEntry> list = dataService.getTree(repository, treeSha).getTree();
            // Position of Posts.
            String pPos = null;
            // Position of drafts.
            String dPos = null;

            for (TreeEntry aList : list) {
                if (aList.getPath().equals("_posts"))
                    pPos = aList.getSha();
                if (aList.getPath().equals("_drafts"))
                    dPos = aList.getSha();
            }

            List<TreeEntry> postslist = dataService.getTree(repository, pPos).getTree();
            List<TreeEntry> draftslist = dataService.getTree(repository, dPos).getTree();
            getPostDataFromList(repository, postslist, 0);
            getPostDataFromList(repository, draftslist, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}
