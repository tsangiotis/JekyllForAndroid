package gr.tsagi.jekyllforandroid.github;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.utils.BusProvider;

/**
 * Created by tsagi on 1/30/14.
 */

public class GithubPush {

    private String user;
    private String token;
    private String repo;
    private String dir;
    private String json;
    private String jsonPath;

    public GithubPush(Context context){
        SharedPreferences settings = context
                .getSharedPreferences("gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        user = settings.getString("user_login", "");
        token = settings.getString("user_status", "");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        dir = sharedPref.getString("posts_subdir", "");
        if(!dir.equals(""))
            dir = dir +"/";
        repo = settings.getString("user_repo", "");
        jsonPath = context.getResources()
                .getString(R.string.json_path);
    }

    public void pushJson() throws ExecutionException, InterruptedException {
        json ="---\n" +
                "title: json" + "\n" +
                "---\n" +
                "{\"homepage\":\"{{ site.production_url }}\",\"name\":\"{{ site.title }}\",\"description\":\"{{ site.tagline }}\""+
                ",\"author\":\"{{ site.author.name }}\",\"posts\":[{% for post in site.posts %}"+
                "{\"url\":\"{{ site.production_url }}{{ post.url }}\",\"title\""+
                ":\"{{ post.title | replace: '\"', '\\\"' }}\",\"id\": \"{{ post.id }}\",\"published_on\":\"{{ post.date | date: \"%-d %B %Y\" }}\"}"+
                "{% if forloop.rindex0 > 0 %},{% endif %}{% endfor %}]}";
        String commitMessage = "Json generator by Jekyll for Android";
        new PushFile().execute(json, jsonPath, commitMessage);
    }

    public void pushContent(String title, String date, String content) throws ExecutionException, InterruptedException {
        // set path
        String path = date + "-" + title.toLowerCase().replace(' ', '-')
                .replace(",","").replace("!","").replace(".","") + ".md";
        path = "_posts/" + dir + path;
        String commitMessage = "Update/new Post from Jekyll for Android";
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
                GitHubClient client = new GitHubClient();
                client.setOAuth2Token(token);

                // create needed services
                RepositoryService repositoryService = new RepositoryService();
                CommitService commitService = new CommitService(client);
                DataService dataService = new DataService(client);

                // get some sha's from current state in git
                Log.d("repository", user + "  " + repo);
                Repository repository =  repositoryService.getRepository(user, repo);
                String baseCommitSha = repositoryService.getBranches(repository).get(0).getCommit().getSha();
                RepositoryCommit baseCommit = commitService.getCommit(repository, baseCommitSha);
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
                Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

                // create commit
                Commit commit = new Commit();
                commit.setMessage(commitMessage);
                commit.setTree(newTree);
                List<Commit> listOfCommits = new ArrayList<Commit>();
                listOfCommits.add(new Commit().setSha(baseCommitSha));
                commit.setParents(listOfCommits);
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
        protected void onPostExecute(String res) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("result", res);
            BusProvider.getInstance().register(this);
            BusProvider.getInstance().post(result);
            BusProvider.getInstance().unregister(this);
        }
    }
}
