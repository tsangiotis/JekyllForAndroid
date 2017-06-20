package gr.tsagi.jekyllforandroid.app.activities

import android.app.AlertDialog
import android.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast

import java.util.ArrayList

import gr.tsagi.jekyllforandroid.app.R
import gr.tsagi.jekyllforandroid.app.adapters.NavDrawerListAdapter
import gr.tsagi.jekyllforandroid.app.data.PostsDbHelper
import gr.tsagi.jekyllforandroid.app.fragments.MarkdownPreviewFragment
import gr.tsagi.jekyllforandroid.app.fragments.PostsListFragment
import gr.tsagi.jekyllforandroid.app.fragments.PrefsFragment
import gr.tsagi.jekyllforandroid.app.utils.FetchPostsTask
import gr.tsagi.jekyllforandroid.app.utils.NavDrawerItem

//import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by tsagi on 9/9/13.
 */
class PostsListActivity : BaseActivity(), PostsListFragment.Callback {

    internal var mUsername: String
    internal var mToken: String
    internal var mRepo: String
    internal var settings: SharedPreferences

    internal var fetchPostsTask: FetchPostsTask? = null

    private var mNavTitles: Array<String>? = null

    private var mDrawerLayout: DrawerLayout? = null
    private val mDrawerToggle: ActionBarDrawerToggle? = null
    private var mDrawerTitle: CharSequence? = null
    private var mTitle: CharSequence? = null

    internal var create: ImageButton

    private var mDrawerList: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActionBarIcon(R.drawable.ic_ab_drawer)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // create our manager instance after the content view is set
            //            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            //            tintManager.setStatusBarTintEnabled(true);
            // Set color
            //            tintManager.setTintColor(getResources().getColor(R.color.primary));
        }

        restorePreferences()
        DrawerSetup()

        if (mToken == "") {
            login()
        } else {
            updateList()
            selectItem(0)
            if (findViewById(R.id.markdown_preview_container) != null) {
                // The preview container view will be present only in the large-screen layouts
                // (res/layout-sw600dp). If this view is present, then the activity should be
                // in two-pane mode.
                mTwoPane = true
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.markdown_preview_container, MarkdownPreviewFragment())
                            .commit()
                }
            } else {
                mTwoPane = false
            }
        }

        if (mRepo == "" && mToken != "") {
            Toast.makeText(this@PostsListActivity,
                    "There is something wrong with your jekyll repo",
                    Toast.LENGTH_LONG).show()
        }

        create = findViewById(R.id.fab) as ImageButton
        create.setOnClickListener { newPost() }

    }

    protected override val layoutResource: Int
        get() = R.layout.activity_posts_list

    private fun updateList() {
        //        fetchPostsTask = new FetchPostsTask(this, logview);
        //        fetchPostsTask.execute();
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //        mDrawerToggle.syncState();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar
        // if it is present.
        menuInflater.inflate(R.menu.main, menu)

        // Just for the logout
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle!!.onOptionsItemSelected(item))
            return true
        val i = item.itemId
        if (i == R.id.action_logout) {
            logoutDialog()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // If the nav drawer is open,
        // hide action items related to the content view
        return super.onPrepareOptionsMenu(menu)
    }

    private fun DrawerSetup() {
        mNavTitles = resources.getStringArray(R.array.nav_array)
        mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        mDrawerList = findViewById(R.id.left_drawer) as ListView

        mDrawerTitle = resources.getString(R.string.app_name)

        val navMenuIcons = resources
                .obtainTypedArray(R.array.nav_drawer_icons_dark)
        val navDrawerItems: ArrayList<NavDrawerItem>
        val adapter: NavDrawerListAdapter

        navDrawerItems = ArrayList<NavDrawerItem>()

        navDrawerItems.add(NavDrawerItem(mNavTitles!![0],
                navMenuIcons.getResourceId(0, -1)))
        navDrawerItems.add(NavDrawerItem(mNavTitles!![1],
                navMenuIcons.getResourceId(1, -1)))
        navDrawerItems.add(NavDrawerItem(mNavTitles!![2],
                navMenuIcons.getResourceId(2, -1)))

        navMenuIcons.recycle()

        adapter = NavDrawerListAdapter(applicationContext,
                navDrawerItems)
        mDrawerList!!.adapter = adapter

        val toolbar = findViewById(R.id.toolbar) as AppBarLayout

        // Set the list's click listener
        mDrawerList!!.setOnItemClickListener(DrawerItemClickListener())
        //        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
        //                toolbar, R.string.drawer_open,
        //                R.string.drawer_close) {
        //
        //            /** Called when a drawer has settled in a completely closed state.*/
        //            public void onDrawerClosed(View view) {
        //                super.onDrawerClosed(view);
        //                getSupportActionBar().setTitle(mTitle);
        //                supportInvalidateOptionsMenu(); // creates call onPrepareOptionsMenu()
        //            }
        //
        //            /** Called when a drawer has settled in a completely open state. */
        //            public void onDrawerOpened(View drawerView) {
        //                super.onDrawerOpened(drawerView);
        //                getSupportActionBar().setTitle(mDrawerTitle);
        //                supportInvalidateOptionsMenu(); // creates call onPrepareOptionsMenu()
        //            }
        //        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)

        //        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // just styling option add shadow the right edge of the drawer
        mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START)
    }

    override fun onItemSelected(postId: String, content: String, postStatus: Int) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            val args = Bundle()
            args.putString(PreviewMarkdownActivity.POST_CONTENT, content)

            val fragment = MarkdownPreviewFragment()
            fragment.arguments = args

            supportFragmentManager.beginTransaction()
                    .replace(R.id.markdown_preview_container, fragment)
                    .commit()
        } else {
            editPost(postId, postStatus)
        }
    }

    override fun onItemEditSelected(postId: String, content: String, postStatus: Int) {
        editPost(postId, postStatus)
    }

    override fun onItemDeleteSelected(postId: String, content: String, postStatus: Int) {

    }

    private inner class DrawerItemClickListener : ListView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View,
                                 position: Int, id: Long) {
            selectItem(position)
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private fun selectItem(position: Int) {

        var fragment: Fragment? = null
        val data = Bundle()
        data.putInt(PostsListActivity.POST_STATUS, position)

        when (position) {
            0 -> try {
                fragment = PostsListFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            1 -> try {
                fragment = PostsListFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            2 -> try {
                fragment = PrefsFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        // Insert the fragment by replacing any existing fragment

        assert(fragment != null)
        fragment!!.arguments = data
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit()

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList!!.setItemChecked(position, true)
        setTitle(mNavTitles!![position])
        mDrawerLayout!!.closeDrawer(mDrawerList)

    }

    override fun setTitle(title: CharSequence) {
        mTitle = title
        //        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * Start new post or continue working on your draft
     */
    fun newPost() {
        val myIntent = Intent(this@PostsListActivity,
                EditPostActivity::class.java)
        startActivity(myIntent)
    }

    fun editPost(postId: String, postStatus: Int) {
        val intent = Intent(this, EditPostActivity::class.java)
                .putExtra(EditPostActivity.POST_ID, postId)
                .putExtra(EditPostActivity.POST_STATUS, postStatus)
        startActivity(intent)
    }

    /**
     * Logout and clear settings
     */
    fun logoutDialog() {
        val builder = AlertDialog.Builder(this)

        // Shared preferences and Intent settings
        // before logout ask user and remind him any draft posts

        val sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE)

        if (sharedPreferences.getString("draft_content", "") == "")
            builder.setMessage(R.string.dialog_logout_nodraft)
        else
            builder.setMessage(R.string.dialog_logout_draft)

        // Add the buttons
        builder.setPositiveButton(R.string.ok
        ) { dialog, id ->
            // User clicked OK button
            // Clear credentials and Drafts
            login()
        }
        builder.setNegativeButton(R.string.cancel
        ) { dialog, id ->
            // User cancelled the dialog
        }

        // Create the AlertDialog
        val dialog = builder.create()

        // Show it
        dialog.show()

    }

    private fun restorePreferences() {
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE)
        mUsername = settings.getString("user_login", "")
        mToken = settings.getString("user_status", "")
        mRepo = settings.getString("user_repo", "")

    }

    private fun login() {
        val PostListIntent = Intent(this@PostsListActivity,
                LoginActivity::class.java)
        val sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.clear()
        editor.commit()

        val db = PostsDbHelper(this)
        db.dropTables()
        db.close()

        startActivity(PostListIntent)
        this.finish()

    }

    companion object {

        private val LOG_TAG = PostsListActivity::class.java.simpleName

        var mTwoPane: Boolean = false

        val POST_STATUS = "post_status"
    }
}

