package gr.tsagi.jekyllforandroid.app.fragments

import android.annotation.TargetApi
import android.app.Fragment
import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.BaseColumns
import android.support.v7.app.ActionBarActivity
import android.support.v7.view.ActionMode
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import gr.tsagi.jekyllforandroid.app.R
import gr.tsagi.jekyllforandroid.app.activities.PostsListActivity
import gr.tsagi.jekyllforandroid.app.adapters.PostListAdapter
import gr.tsagi.jekyllforandroid.app.data.PostsContract.PostEntry


/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 7/5/14
\* Time: 19:49
\*/
class PostsListFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private var mPostListAdapter: PostListAdapter? = null

    private var status = -1

    private var mActionMode: ActionMode? = null
    private var mListView: ListView? = null
    private var mPosition = ListView.INVALID_POSITION

    private var postid: String? = null
    private var pstatus: Int = 0
    private var content: String? = null

    init {
        // Empty constructor required for fragment subclasses
        setHasOptionsMenu(true)
    }

    private val mActionModeCallback = object : ActionMode.Callback {

        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.context_menu, menu)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val i = item.itemId
            if (i == R.id.action_edit) {
                (activity as Callback)
                        .onItemEditSelected(postid!!, content!!, pstatus)
                mode.finish() // Action picked, so close the CAB
                return true
            } else if (i == R.id.action_delete) {
                (activity as Callback)
                        .onItemDeleteSelected(postid!!, content!!, pstatus)
                mode.finish() // Action picked, so close the CAB
                return true
            } else {
                return false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
        }
    }

    interface Callback {
        /**
         * PreviewCallback for when an item has been selected.
         */
        fun onItemSelected(postId: String, content: String, postStatus: Int)

        fun onItemEditSelected(postId: String, content: String, postStatus: Int)

        fun onItemDeleteSelected(postId: String, content: String, postStatus: Int)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val arguments = arguments
        if (arguments != null) {
            status = arguments.getInt(PostsListActivity.POST_STATUS)
        }
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mPostListAdapter = PostListAdapter(activity, null, 0)

        // Set the first position as the default
        mPosition = 0

        val rootView = inflater.inflate(R.layout.fragment_posts_list,
                container, false)

        mListView = rootView.findViewById<View>(R.id.listview_postslist) as ListView
        mListView!!.adapter = mPostListAdapter
        mListView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, l ->
            val cursor = mPostListAdapter!!.cursor
            if (cursor != null && cursor.moveToPosition(position)) {
                val postid = cursor.getString(COL_POST_ID)
                val pstatus = cursor.getInt(COL_POST_DRAFT)
                val content = cursor.getString(COL_POST_CONTENT)
                (activity as Callback)
                        .onItemSelected(postid, content, pstatus)

            }
        }

        mListView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, position, l ->
            if (mActionMode != null) {
                return@OnItemLongClickListener false
            }

            view.isSelected = true

            val cursor = mPostListAdapter!!.cursor
            if (cursor != null && cursor.moveToPosition(position)) {
                postid = cursor.getString(COL_POST_ID)
                pstatus = cursor.getInt(COL_POST_DRAFT)
                content = cursor.getString(COL_POST_CONTENT)

                // Start the CAB using the ActionMode.Callback defined above
                val activity = activity as ActionBarActivity
                mActionMode = activity.startSupportActionMode(mActionModeCallback)
                view.isSelected = true
                return@OnItemLongClickListener true
            }
            false
        }


        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY)
        }

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
        //            case R.id.drop_tables:
        //                PostsDbHelper db = new PostsDbHelper(getActivity());
        //                db.dropTables();
        //                db.close();
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        loaderManager.initLoader(LIST_LOADER, null, this)
        super.onActivityCreated(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        loaderManager.restartLoader(LIST_LOADER, null, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.

        // Sort order:  Descending, by date.
        val sortOrder = PostEntry.COLUMN_POST_ID + " DESC"
        val postsUri: Uri

        if (status == 0)
            postsUri = PostEntry.buildPublishedPosts()
        else
            postsUri = PostEntry.buildDraftPosts()

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return CursorLoader(
                activity,
                postsUri,
                POSTS_COLUMNS, null, null,
                sortOrder
        )
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mPostListAdapter!!.swapCursor(data)
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                mListView!!.smoothScrollToPosition(mPosition)
        }

        if (mPostListAdapter!!.count == 0) {
            Toast.makeText(activity, "You have nothing to display here. :)",
                    Toast.LENGTH_SHORT).show()
        } else {
            // select the first post and render it.
            if (PostsListActivity.mTwoPane) {

                Handler().post {
                    mPostListAdapter!!.notifyDataSetChanged()
                    mListView!!.performItemClick(
                            mListView!!.getChildAt(0),
                            0,
                            mListView!!.adapter.getItemId(mListView!!.adapter.count))
                }

            }
        }

    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mPostListAdapter!!.swapCursor(null)
    }

    companion object {

        private val LOG_TAG = PostsListFragment::class.java.simpleName

        private val SELECTED_KEY = "selected_position"

        private val LIST_LOADER = 0

        // For the forecast view we're showing only a small subset of the stored data.
        // Specify the columns we need.
        private val POSTS_COLUMNS = arrayOf(
                // In this case the id needs to be fully qualified with a table name, since
                // the content provider joins the location & weather tables in the background
                // (both have an _id column)
                // On the one hand, that's annoying.  On the other, you can search the weather table
                // using the location set by the user, which is only in the Location table.
                // So the convenience is worth it.
                PostEntry.TABLE_NAME + "." + BaseColumns._ID, PostEntry.COLUMN_POST_ID, PostEntry.COLUMN_TITLE, PostEntry.COLUMN_DATETEXT, PostEntry.COLUMN_CONTENT, PostEntry.COLUMN_DRAFT)


        // These indices are tied to POSTS_COLUMNS. If POSTS_COLUMNS changes, these must change.
        val COL_ID = 0
        val COL_POST_ID = 1
        val COL_POST_TITLE = 2
        val COL_POST_DATE = 3
        val COL_POST_CONTENT = 4
        val COL_POST_DRAFT = 5
    }


}
