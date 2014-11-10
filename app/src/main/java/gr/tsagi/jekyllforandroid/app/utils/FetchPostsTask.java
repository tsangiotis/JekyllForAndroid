package gr.tsagi.jekyllforandroid.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import gr.tsagi.jekyllforandroid.app.data.PostsContract;

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
    UserService userService;

    Utility utility;

    String baseCommitSha = "";

    public FetchPostsTask(Context context) {

        mContext = context;

        utility = new Utility(mContext);

        final String token = utility.getToken();

        // Start the client
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);

        // Initiate services
        repositoryService = new RepositoryService(client);
        commitService = new CommitService(client);
        dataService = new DataService(client);
        userService = new UserService(client);
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
                String[] filenameParts = filename.split("\\.");
                String id = filenameParts[0];

                if (id.equals("")) {
                    Log.d(LOG_TAG, "No id...");
                    continue;
                }

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
                if (postValues.size() > 0){
                    Log.d(LOG_TAG, "Values for: " +id);
                    contentValuesVector.add(postValues);
                }

            } else {
                try {
                    List<TreeEntry> subdir = dataService.getTree(repository, post.getSha()).getTree();
                    getPostDataFromList(repository, subdir, type);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // this should be outside the loop as it otherwise produces duplicates
        if (contentValuesVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(PostsContract.PostEntry.CONTENT_URI, cvArray);
            Log.d(LOG_TAG, "Inserted Values.");
        } else {
            Log.d(LOG_TAG, "No Values to insert.");
        }
    }

    @Override
    protected Void doInBackground(String... params) {

        Log.d(LOG_TAG, "Background started");

        // TODO: Support subdirectories
        final String user = utility.getUser();
        final String repo = utility.getRepo();

        try {
            String avatar = userService.getUser().getAvatarUrl();
            Log.d(LOG_TAG, "Avatar: " + avatar);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get some sha's from current state in git
        Log.d(LOG_TAG, user + " - " + repo);
        Repository repository;

        try {
            repository = repositoryService.getRepository(user, repo);

            // maybe the user has many branches
            List<RepositoryBranch> branchList = repositoryService.getBranches(repository);
            for (int i = 0; i <= branchList.size(); i++) {
                String name = branchList.get(i).getName();
                if (name.equals("master")) {
                    baseCommitSha = repositoryService.getBranches(repository).get(i)
                            .getCommit()
                            .getSha();
                    break;
                }
            }

            // No sync when the same sha.
            String oldSha = utility.getBaseCommitSha();

            if (baseCommitSha.equals(oldSha)) {
                Log.d(LOG_TAG, "No Sync");
                this.cancel(true);
                return null;
            }

            final String treeSha = commitService.getCommit(repository, baseCommitSha).getSha();

            List<TreeEntry> paths = dataService.getTree(repository, treeSha).getTree();
            // Position of Posts.
            String pPos = "";
            // Position of drafts.
            String dPos = "";

            for (TreeEntry aPath : paths) {

                Log.d(LOG_TAG, aPath.getPath());
                if (aPath.getPath().equals("_posts")) {
                    Log.d(LOG_TAG, "Found posts!");
                    pPos = aPath.getSha();
                }
                if (aPath.getPath().equals("_drafts")) {
                    Log.d(LOG_TAG, "Found drafts!");
                    dPos = aPath.getSha();
                }
            }

            if (!pPos.equals("")) {
                List<TreeEntry> postslist = dataService.getTree(repository, pPos).getTree();
                getPostDataFromList(repository, postslist, 0);
            }
            if (!dPos.equals("")) {
                List<TreeEntry> draftslist = dataService.getTree(repository, dPos).getTree();
                getPostDataFromList(repository, draftslist, 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        utility.setBaseCommitSha(baseCommitSha);
    }
}
