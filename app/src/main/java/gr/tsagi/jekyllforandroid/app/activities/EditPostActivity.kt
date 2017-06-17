package gr.tsagi.jekyllforandroid.app.activities

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NavUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import app.wt.noolis.R
import com.readystatesoftware.systembartint.SystemBarTintManager
import gr.tsagi.jekyllforandroid.app.data.PostsContract
import gr.tsagi.jekyllforandroid.app.utils.GithubPush
import gr.tsagi.jekyllforandroid.app.utils.Utility
import org.yaml.snakeyaml.Yaml
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException

open class EditPostActivity : BaseActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private var mPostId: String? = null
    private var mPostStatus: Int = 0

    private var mTitle: EditText? = null
    private var mTags: EditText? = null
    private var mCategory: EditText? = null
    private var mContent: EditText? = null

    lateinit internal var publish: ImageButton

    lateinit internal var utility: Utility

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // create our manager instance after the content view is set
            val tintManager = SystemBarTintManager(this)
            // enable status bar tint
            tintManager.isStatusBarTintEnabled = true
            // Set color
            tintManager.setTintColor(resources.getColor(R.color.primary))
        }

        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
        mPostId = intent.getStringExtra(POST_ID)
        mPostStatus = intent.getIntExtra(POST_STATUS, -1)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        utility = Utility(this)

        mTitle = findViewById(R.id.edit_title) as EditText
        mTags = findViewById(R.id.edit_tags) as EditText
        mCategory = findViewById(R.id.edit_category) as EditText
        mContent = findViewById(R.id.edit_content) as EditText

        publish = findViewById(R.id.fab) as ImageButton
        publish.setOnClickListener { publishPost() }

    }

    override val layoutResource: Int
        get() = R.layout.activity_edit_post

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar
        // if it is present.
        menuInflater.inflate(R.menu.post, menu)

        // Just for the logout
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            R.id.action_draft -> {
                uploadDraft()
                return true
            }
            R.id.action_preview -> {
                previewMarkdown()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    public override fun onResume() {
        super.onResume()
        if (mPostId != null) {
            loaderManager.restartLoader(EDIT_POST_LOADER, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        // Sort order:  Descending, by date.

        Log.d(LOG_TAG, "postId: " + mPostId!!)

        val postFromId: Uri
        if (mPostStatus == 1) {
            postFromId = PostsContract.PostEntry.buildPostFromId("draft", mPostId!!)
        } else {
            postFromId = PostsContract.PostEntry.buildPostFromId("published", mPostId!!)
        }

        Log.d(LOG_TAG, "postIdUri: " + postFromId.toString())

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return CursorLoader(
                this,
                postFromId,
                POST_COLUMNS, null, null, null
        )
    }

    override fun onLoadFinished(loader: android.content.Loader<Cursor>, data: Cursor?) {
        if (data != null && data.moveToFirst()) {
            // Read title from cursor
            val title = data.getString(data.getColumnIndex(PostsContract.PostEntry.COLUMN_TITLE))
            mTitle!!.setText(title)

            //            String tags = data.getString(data.getColumnIndex(PostsContract.TagEntry.COLUMN_TAG));
            //            Log.d(LOG_TAG, "tags:" + tags);
            //            if(!tags.equals("null"))
            //                mTags.setText(tags);

            //            String category = data.getString(data.getColumnIndex(PostsContract.CategoryEntry.COLUMN_CATEGORY));
            //            Log.d(LOG_TAG, "category: " + category);
            //            if(!category.equals("null"))
            //                mCategory.setText(category);

            val content = data.getString(data.getColumnIndex(PostsContract.PostEntry.COLUMN_CONTENT))
            mContent!!.setText(content)
        }
    }

    override fun onLoaderReset(loader: android.content.Loader<Cursor>) {

    }

    private fun uploadDraft() {

        val title = mTitle!!.text.toString().trim { it <= ' ' }
        val tags = mTags!!.text.toString().trim { it <= ' ' }
        val category = mCategory!!.text.toString().trim { it <= ' ' }
        val content = mContent!!.text.toString().trim { it <= ' ' }

        if (content == "")
            Toast.makeText(this, R.string.editpost_empty, Toast.LENGTH_LONG).show()
        else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialog_confirm_draft)
            // Add the buttons
            builder.setPositiveButton(R.string.ok) { dialog, id -> pushDraft(title, tags, category, content) }
            builder.setNegativeButton(R.string.cancel) { dialog, id ->
                // User cancelled the dialog
            }

            // Create the AlertDialog
            val dialog = builder.create()
            dialog.show()
        }

    }

    fun pushDraft(title: String, tags: String, category: String, content: String) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val data = HashMap<String, Any>()

        val yaml = Yaml()
        val customYaml = prefs.getString("yaml_values", "")
        Log.d(LOG_TAG, customYaml)
        val map = yaml.load(customYaml) as HashMap<String, Any>

        data.put("title", title)
        data.put("tags", tags.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        data.put("category", category)
        data.put("layout", "post")
        if (map != null)
            data.putAll(map)

        val output = "---\n" + yaml.dump(data) + "---\n"

        val pusher = GithubPush(this)

        try {
            pusher.pushDraft(title, output + content)
            Log.d(LOG_TAG, title + " " + content)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    fun pushPost(title: String, tags: String, category: String, content: String) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val data = HashMap<String, Any>()
        val date = SimpleDateFormat("yyyy-MM-dd").format(Date())

        val yaml = Yaml()
        val customYaml = prefs.getString("yaml_values", "")
        Log.d("yaml", customYaml)
        val map = yaml.load(customYaml) as HashMap<String, Any>
        data.put("tags", tags.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        data.put("category", category)
        data.put("title", title)
        data.put("layout", "post")
        if (map != null)
            data.putAll(map)

        val output = "---\n" + yaml.dump(data) + "---\n"

        val pusher = GithubPush(this)

        try {
            pusher.pushContent(title, date, output + content)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }


    private fun publishPost() {

        val title = mTitle!!.text.toString().trim { it <= ' ' }
        val tags = mTags!!.text.toString().trim { it <= ' ' }
        val category = mCategory!!.text.toString().trim { it <= ' ' }
        val content = mContent!!.text.toString().trim { it <= ' ' }

        if (content == "")
            Toast.makeText(this, R.string.editpost_empty, Toast.LENGTH_LONG).show()
        else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialog_confirm_update)
            // Add the buttons
            builder.setPositiveButton(R.string.ok) { dialog, id -> pushPost(title, tags, category, content) }
            builder.setNegativeButton(R.string.cancel) { dialog, id ->
                // User cancelled the dialog
            }

            // Create the AlertDialog
            val dialog = builder.create()
            dialog.show()
        }
    }


    fun previewMarkdown() {

        val content = mContent!!.text.toString().trim { it <= ' ' }
        val repo = utility.repo

        if (content != "") {
            val myIntent = Intent(this, PreviewMarkdownActivity::class.java)
            myIntent.putExtra(PreviewMarkdownActivity.POST_CONTENT, content)
            startActivity(myIntent)
        } else
            Toast.makeText(this, "Nothing to preview", Toast.LENGTH_SHORT).show()
    }

    companion object {

        private val LOG_TAG = EditPostActivity::class.java.simpleName

        private val EDIT_POST_LOADER = 0

        private val POST_COLUMNS = arrayOf(PostsContract.PostEntry.COLUMN_POST_ID, PostsContract.PostEntry.COLUMN_TITLE, PostsContract.PostEntry.COLUMN_CONTENT)//            PostsContract.TagEntry.COLUMN_TAG,
        //            PostsContract.CategoryEntry.COLUMN_CATEGORY

        val POST_ID = "post_id"
        val POST_STATUS = "post_status"
    }

}