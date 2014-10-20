package gr.tsagi.jekyllforandroid.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by tsagi on 1/30/14.
 */

public class GithubPush {

    private String user;
    private String token;
    private String repo;
    private String dir;

    Activity mActivity;

    public GithubPush(Activity activity){
        this.mActivity=activity;
        SharedPreferences settings = mActivity
                .getSharedPreferences("gr.tsagi.jekyllforandroid",
                        Context.MODE_PRIVATE);
        user = settings.getString("user_login", "");
        token = settings.getString("user_status", "");
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(mActivity);
        dir = sharedPref.getString("posts_subdir", "");
        if(!dir.equals(""))
            dir = dir +"/";
        repo = settings.getString("user_repo", "");
    }

    public void pushContent(String title, String date, String content) throws
            ExecutionException, InterruptedException {
        // set path
        String path = date + "-" + title.toLowerCase().replace(' ', '-')
                .replace(",","").replace("!","").replace(".","") + ".md";
        path = "_posts/" + dir + path;
        String commitMessage = "Update/New Post from Jekyll for Android";
        new PushFile().execute(content, path, commitMessage);

    }

    public void pushDraft(String title, String content) throws
            ExecutionException, InterruptedException {
        // set path
        String path = title.toLowerCase().replace(' ', '-')
                .replace(",","").replace("!","").replace(".","") + ".md";
        path = "_drafts/" + dir + path;
        String commitMessage = "Update/New Draft from Jekyll for Android";
        new PushFile().execute(content, path, commitMessage);

    }

    class PushFile extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            try {

                String blobContent = params[0];
                String path = params[1];
                String commitMessage = params[2];

                // based on http://swanson.github.com/blog/2011/07/23/digging-around-the-github-api-take-2.html
                // initialize github client
                GitHubClient client = new GitHubClient().setOAuth2Token(token);

                // create needed services
                RepositoryService repositoryService = new RepositoryService();
                CommitService commitService = new CommitService(client);
                DataService dataService = new DataService(client);

                // get some sha's from current state in git
                Log.d("repository", user + " " + repo);
                Repository repository =  repositoryService
                        .getRepository(user, repo);
                String baseCommitSha = repositoryService
                        .getBranches(repository).get(0).getCommit().getSha();
                RepositoryCommit baseCommit = commitService
                        .getCommit(repository, baseCommitSha);
                String treeSha = baseCommit.getSha();


                // create new blob with data
                Blob blob = new Blob();
                blob.setContent(blobContent).setEncoding(Blob.ENCODING_UTF8);
                String blob_sha = dataService.createBlob(repository, blob);
                Tree baseTree = dataService.getTree(repository, treeSha);

                // create new tree entry
                TreeEntry treeEntry = new TreeEntry();

                Log.d("RepoPath", path);

                // working
                treeEntry.setPath(path);
                treeEntry.setMode(TreeEntry.MODE_BLOB);
                treeEntry.setType(TreeEntry.TYPE_BLOB);
                treeEntry.setSha(blob_sha);
                treeEntry.setSize(blob.getContent().length());
                Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
                entries.add(treeEntry);
                Tree newTree = dataService.createTree(repository,
                        entries, baseTree.getSha());

                // create commit
                Commit commit = new Commit();
                commit.setMessage(commitMessage);
                commit.setTree(newTree);

                //Due to an error with github api we have to to all this
                //TODO: Make this better (another function)
                UserService userService = new UserService(client);
                User user = userService.getUser();
                CommitUser author = new CommitUser();
                author.setName(user.getName());
                author.setEmail(userService.getEmails().get(0));
                Calendar now = Calendar.getInstance();
                author.setDate(now.getTime());
                commit.setAuthor(author);
                commit.setCommitter(author);

                List<Commit> listOfCommits = new ArrayList<Commit>();
                listOfCommits.add(new Commit().setSha(baseCommitSha));
                commit.setParents(listOfCommits);
                Log.d("commit", commit.getMessage());
                Commit newCommit = dataService.createCommit(repository, commit);

                // create resource
                TypedResource commitResource = new TypedResource();
                commitResource.setSha(newCommit.getSha());
                commitResource.setType(TypedResource.TYPE_COMMIT);
                commitResource.setUrl(newCommit.getUrl());

                // get master reference and update it
                Reference reference = dataService.getReference(repository, "heads/master");
                reference.setObject(commitResource);
                dataService.editReference(repository, reference, true);

                // success

            } catch (Exception e) {
                // error
                e.printStackTrace();
                return "error";
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mActivity.finish();
        }
    }
}
