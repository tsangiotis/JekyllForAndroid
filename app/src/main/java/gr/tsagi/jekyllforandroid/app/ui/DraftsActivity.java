package gr.tsagi.jekyllforandroid.app.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import gr.tsagi.jekyllforandroid.app.R;
import gr.tsagi.jekyllforandroid.app.data.PostsDatabase;
import gr.tsagi.jekyllforandroid.app.utils.FetchAvatar;
import gr.tsagi.jekyllforandroid.app.utils.FetchPostsTask;
import gr.tsagi.jekyllforandroid.app.utils.Utility;

/**
 * Created by tsagi on 9/9/13.
 */

public class DraftsActivity extends BaseActivity implements PostsListFragment.Callback {

    private static final String LOG_TAG = DraftsActivity.class.getSimpleName();

    // symbols for navdrawer items (indices must correspond to array below). This is
    // not a list of items that are necessarily *present* in the Nav Drawer; rather,
    // it's a list of all possible items.
    protected static final int NAVDRAWER_ITEM_POSTS = 0;
    protected static final int NAVDRAWER_ITEM_DRAFTS = 1;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 2;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navdrawer_item_posts,
            R.string.navdrawer_item_drafts,
            R.string.navdrawer_item_settings,
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[] {
            R.drawable.ic_drawer_posts,  // Posts
            R.drawable.ic_drawer_drafts,  // Drafts
            R.drawable.ic_drawer_settings, // Settings
    };

    public static boolean mTwoPane;

    public static final String POST_STATUS = "post_status";

    String mUsername;
    String mToken;
    String mRepo;
    SharedPreferences settings;

    FetchPostsTask fetchPostsTask;

    private String[] mNavTitles;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // Primary toolbar and drawer toggle
    private Toolbar mActionBarToolbar;

    ImageButton create;

    private ListView mDrawerList;
    private View mDrawerView;

    private ViewGroup mDrawerItemsListContainer;

    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;

    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utility = new Utility(this);

        restorePreferences();

        if (mToken.equals("")) {
            Log.d(LOG_TAG, "Login");
            login();
        } else {
            Log.d(LOG_TAG, "Loged in");
            setActionBarIcon(R.drawable.ic_ab_drawer);

            updateList();
            selectItem(0);
            if (findViewById(R.id.markdown_preview_container) != null) {
                // The preview container view will be present only in the large-screen layouts
                // (res/layout-sw600dp). If this view is present, then the activity should be
                // in two-pane mode.
                mTwoPane = true;
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.
                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.markdown_preview_container, new MarkdownPreviewFragment())
                            .commit();
                }
            } else {
                mTwoPane = false;
            }
        }

        if (mRepo.equals("") && !mToken.equals("")) {
            Toast.makeText(DraftsActivity.this,
                    "There is something wrong with your jekyll repo",
                    Toast.LENGTH_LONG).show();
        }

        create = (ImageButton) findViewById(R.id.fab);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                newPost();
            }
        });

    }

    @Override protected int getLayoutResource() {
        return R.layout.activity_posts_list;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    private void updateList() {
        new FetchAvatar(this).execute();

        fetchPostsTask = new FetchPostsTask(this);
        fetchPostsTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        // if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Just for the logout
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open,
        // hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onItemSelected(String postId, String content, int postStatus) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(PreviewMarkdownActivity.POST_CONTENT, content);

            MarkdownPreviewFragment fragment = new MarkdownPreviewFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.markdown_preview_container, fragment)
                    .commit();
        } else {
            editPost(postId, postStatus);
        }
    }

    @Override
    public void onItemEditSelected(String postId, String content, int postStatus) {
        editPost(postId, postStatus);
    }

    @Override
    public void onItemDeleteSelected(String postId, String content, int postStatus) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view,
                                int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        Fragment fragment = null;
        Bundle data = new Bundle();
        data.putInt(DraftsActivity.POST_STATUS, position);

        switch (position) {
            case 0:
                try {
                    fragment = new PostsListFragment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    fragment = new PostsListFragment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    fragment = new PrefsFragment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

        // Insert the fragment by replacing any existing fragment

        assert fragment != null;
        fragment.setArguments(data);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mNavTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerView);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * Start new post or continue working on your draft
     */
    public void newPost() {
        EditPostActivity.launch(DraftsActivity.this,
                findViewById(R.id.fab), "new", 3);
    }

    public void editPost(String postId, int postStatus) {
        EditPostActivity.launch(DraftsActivity.this,
               findViewById(R.id.fab), postId, postStatus);
    }

    /**
     * Logout and clear settings
     */
    public void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Shared preferences and Intent settings
        // before logout ask user and remind him any draft posts

        final SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", MODE_PRIVATE);

        if (sharedPreferences.getString("draft_content", "").equals(""))
            builder.setMessage(R.string.dialog_logout_nodraft);
        else
            builder.setMessage(R.string.dialog_logout_draft);

        // Add the buttons
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        // Clear credentials and Drafts
                        login();
                    }
                }
        );
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }
        );

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show it
        dialog.show();

    }

    private void restorePreferences() {
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", MODE_PRIVATE);
        mUsername = settings.getString("user_login", "");
        mToken = settings.getString("user_status", "");
        mRepo = settings.getString("user_repo", "");

    }

    private void login() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();

        PostsDatabase db = new PostsDatabase(this);
        db.dropTables();
        db.close();

        Log.d(LOG_TAG, "Launching login");
        LoginActivity.launch(DraftsActivity.this, findViewById(R.id.fab));
        finish();

    }

    public static void launch(BaseActivity activity, View transitionView) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, "fab_create");
        Intent intent = new Intent(activity, DraftsActivity.class);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
}

