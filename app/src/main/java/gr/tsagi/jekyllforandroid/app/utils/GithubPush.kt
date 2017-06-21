package gr.tsagi.jekyllforandroid.app.utils

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import org.eclipse.egit.github.core.*
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.DataService
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.egit.github.core.service.UserService
import java.util.*
import java.util.concurrent.ExecutionException

/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 1/30/14
\* Time: 19:54
\*/

class GithubPush(mActivity: Activity) {

    private val user: String
    private val token: String
    private val repo: String
    private var dir: String? = null

    init {
        val settings = mActivity
                .getSharedPreferences("gr.tsagi.jekyllforandroid",
                        Context.MODE_PRIVATE)
        user = settings.getString("user_login", "")
        token = settings.getString("user_status", "")
        val sharedPref = PreferenceManager
                .getDefaultSharedPreferences(mActivity)
        dir = sharedPref.getString("posts_subdir", "")
        Log.i("changhong", user + dir!!)
        if (dir != "")
            dir = dir!! + "/"
        repo = settings.getString("user_repo", "")
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun pushContent(title: String, date: String, content: String, category: String,parent:String?) {
        // set path
        var path = date + "-" + title.toLowerCase().replace(' ', '-')
                .replace(",", "").replace("!", "").replace(".", "") + ".md"
//        path = "_posts/" + dir + path
        if (parent == "") {
            path = "_posts/$category/$path"
        }
        else{
            path = "_posts/$parent/$path"
        }
        val commitMessage = "Update/New Post from Jekyll for Android"
        PushFile().execute(content, path, commitMessage)

    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun pushDraft(title: String, content: String) {
        // set path
        var path = title.toLowerCase().replace(' ', '-')
                .replace(",", "").replace("!", "").replace(".", "") + ".md"
        path = "_drafts/" + dir + path
        val commitMessage = "Update/New Draft from Jekyll for Android"
        PushFile().execute(content, path, commitMessage)

    }

    internal inner class PushFile : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            try {

                val blobContent = params[0]
                val path = params[1]
                val commitMessage = params[2]

                // based on http://swanson.github.com/blog/2011/07/23/digging-around-the-github-api-take-2.html
                // initialize github client
                val client = GitHubClient().setOAuth2Token(token)

                // create needed services
                val repositoryService = RepositoryService()
                val commitService = CommitService(client)
                val dataService = DataService(client)

                // get some sha's from current state in git
                Log.d("changhong", user + " " + repo)
                val repository = repositoryService
                        .getRepository(user, repo)
                val baseCommitSha = repositoryService
                        .getBranches(repository)[0].commit.sha
                val baseCommit = commitService
                        .getCommit(repository, baseCommitSha)
                val treeSha = baseCommit.sha


                // create new blob with data
                val blob = Blob()
                blob.setContent(blobContent).encoding = Blob.ENCODING_UTF8
                val blob_sha = dataService.createBlob(repository, blob)
                val baseTree = dataService.getTree(repository, treeSha)

                // create new tree entry
                val treeEntry = TreeEntry()

                Log.d("RepoPath", path)

                // working
                treeEntry.path = path
                treeEntry.mode = TreeEntry.MODE_BLOB
                treeEntry.type = TreeEntry.TYPE_BLOB
                treeEntry.sha = blob_sha
                treeEntry.size = blob.content.length.toLong()
                val entries = ArrayList<TreeEntry>()
                entries.add(treeEntry)
                val newTree = dataService.createTree(repository,
                        entries, baseTree.sha)

                // create commit
                val commit = Commit()
                commit.message = commitMessage
                commit.tree = newTree

                //Due to an error with github api we have to to all this
                //TODO: Make this better (another function)
                val userService = UserService(client)
                val user = userService.user
                val author = CommitUser()
                author.name = user.name
                //                String email = userService.getEmails().get(0).toString();
                //                Log.v("changhong", email);
                author.email = user.email
                val now = Calendar.getInstance()
                author.date = now.time
                commit.author = author
                commit.committer = author
                val listOfCommits = ArrayList<Commit>()
                listOfCommits.add(Commit().setSha(baseCommitSha))
                commit.parents = listOfCommits
                Log.d("commit", commit.message)
                val newCommit = dataService.createCommit(repository, commit)

                // create resource
                val commitResource = TypedResource()
                commitResource.sha = newCommit.sha
                commitResource.type = TypedResource.TYPE_COMMIT
                commitResource.url = newCommit.url

                // get master reference and update it
                val reference = dataService.getReference(repository, "heads/master")
                reference.`object` = commitResource
                dataService.editReference(repository, reference, true)

                // success

            } catch (e: Exception) {
                // error
                e.printStackTrace()
                return "error"
            }

            return "OK"
        }

        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            //            mActivity.finish();
        }
    }
}
