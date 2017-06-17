package gr.tsagi.jekyllforandroid.app.utils

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.util.Log

import org.eclipse.egit.github.core.Blob
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryBranch
import org.eclipse.egit.github.core.TreeEntry
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.DataService
import org.eclipse.egit.github.core.service.RepositoryService

import java.io.IOException
import java.util.Vector

import gr.tsagi.jekyllforandroid.app.data.PostsContract
import gr.tsagi.jekyllforandroid.app.data.PostsDbHelper

/**
 * Created by tsagi on 1/30/14.
 */

class FetchPostsTask(private val mContext: Context) : AsyncTask<String, Void, Void>() {

    private val LOG_TAG = FetchPostsTask::class.java.simpleName

    // Create the needed services
    internal var repositoryService: RepositoryService
    internal var commitService: CommitService
    internal var dataService: DataService

    internal var utility: Utility

    init {

        utility = Utility(mContext)

        val token = utility.token

        // Start the client
        val client = GitHubClient()
        client.setOAuth2Token(token)

        // Initiate services
        repositoryService = RepositoryService()
        commitService = CommitService(client)
        dataService = DataService(client)
    }

    /**
     * Take the List with the posts and parse the posts for data
     */
    private fun getPostDataFromList(repository: Repository, postslist: List<TreeEntry>, type: Int) {

        // Get and insert the new posts information into the database
        val contentValuesVector = Vector<ContentValues>(postslist.size)
        for (post in postslist) {

            if (post.type == "blob") {

                val filename = post.path
                val filenameParts = filename.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val id = filenameParts[0]

                if (id == "") {
                    Log.d(LOG_TAG, "No id...")
                    continue
                }

                val postSha = post.sha
                var postBlob: Blob? = null
                try {
                    postBlob = dataService.getBlob(repository, postSha).setEncoding(Blob.ENCODING_UTF8)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                assert(postBlob != null)
                val blobBytes = postBlob!!.content

                val postValues = ParsePostData(mContext).getDataFromContent(id,
                        blobBytes, type)
                if (postValues.size() > 0) {
                    Log.d(LOG_TAG, "Values for: " + id)
                    contentValuesVector.add(postValues)
                }

            } else {
                try {
                    val subdir = dataService.getTree(repository, post.sha).tree
                    getPostDataFromList(repository, subdir, type)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        // this should be outside the loop as it otherwise produces duplicates
        if (contentValuesVector.size > 0) {
            val cvArray = arrayOfNulls<ContentValues>(contentValuesVector.size)
            contentValuesVector.toTypedArray()
            mContext.contentResolver.bulkInsert(PostsContract.PostEntry.CONTENT_URI, cvArray)
            Log.d(LOG_TAG, "Inserted Values.")
        } else {
            Log.d(LOG_TAG, "No Values to insert.")
        }
    }

    override fun doInBackground(vararg params: String): Void? {

        Log.d(LOG_TAG, "Background started")

        // TODO: Support subdirectories
        val user = utility.user
        val repo = utility.repo


        // get some sha's from current state in git
        Log.d(LOG_TAG, user + " - " + repo)
        val repository: Repository

        try {
            repository = repositoryService.getRepository(user, repo)
            var baseCommitSha = ""

            // maybe the user has many branches
            val branchList = repositoryService.getBranches(repository)
            for (i in 0..branchList.size) {
                val name = branchList[i].name
                if (name == "master") {
                    baseCommitSha = repositoryService.getBranches(repository)[i]
                            .commit
                            .sha
                    break
                }
            }

            // No sync when the same sha.
            val oldSha = utility.baseCommitSha

            if (baseCommitSha == oldSha) {
                Log.d(LOG_TAG, "No Sync")
                this.cancel(true)
                return null
            } else {
                Log.d(LOG_TAG, "Syncing...")
                val db = PostsDbHelper(mContext)
                db.dropTables()
                db.close()
                utility.baseCommitSha = baseCommitSha
            }

            val treeSha = commitService.getCommit(repository, baseCommitSha).sha

            // TODO: Refactor naming here.
            val list = dataService.getTree(repository, treeSha).tree
            // Position of Posts.
            var pPos = ""
            // Position of drafts.
            var dPos = ""

            for (aList in list) {

                Log.d(LOG_TAG, aList.path)
                if (aList.path == "_posts") {
                    Log.d(LOG_TAG, "Found posts!")
                    pPos = aList.sha
                }
                if (aList.path == "_drafts") {
                    Log.d(LOG_TAG, "Found drafts!")
                    dPos = aList.sha
                }
            }

            if (pPos != "") {
                val postslist = dataService.getTree(repository, pPos).tree
                getPostDataFromList(repository, postslist, 0)
            }
            if (dPos != "") {
                val draftslist = dataService.getTree(repository, dPos).tree
                getPostDataFromList(repository, draftslist, 1)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}
