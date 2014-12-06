package gr.tsagi.jekyllforandroid.app.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import gr.tsagi.jekyllforandroid.app.R;
import gr.tsagi.jekyllforandroid.app.data.PostsDbHelper;
import gr.tsagi.jekyllforandroid.app.fragments.MarkdownPreviewFragment;
import gr.tsagi.jekyllforandroid.app.fragments.PostsListFragment;
import gr.tsagi.jekyllforandroid.app.fragments.PrefsFragment;
import gr.tsagi.jekyllforandroid.app.utils.FetchAvatar;
import gr.tsagi.jekyllforandroid.app.utils.FetchPostsTask;
import gr.tsagi.jekyllforandroid.app.utils.Utility;

/**
 * Created by tsagi on 9/9/13.
 */

public class PostsActivity extends BaseActivity implements PostsListFragment.Callback {

    private static final String LOG_TAG = PostsActivity.class.getSimpleName();

    // How is this Activity being used?
    private static final int MODE_POST = 0; // as top-level "Explore" screen

    private int mMode = MODE_POST;

    public static boolean mTwoPane;

    public static final String POST_STATUS = "post_status";

    String mUsername;
    String mToken;
    String mRepo;
    SharedPreferences settings;

    FetchPostsTask fetchPostsTask;
    private CharSequence mTitle;

    ImageButton create;

    Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_posts_list);
        restorePreferences();
        utility = new Utility(this);

        if (mToken.equals("")) {
            Log.d(LOG_TAG, "Login");
            login();
        } else {
            Log.d(LOG_TAG, "Logged in");

            selectItem(0);
            updateList();
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
            Toast.makeText(PostsActivity.this,
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        // if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Just for the logout
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        // we only have a nav drawer if we are in top-level Explore mode.
        return mMode == MODE_POST ? NAVDRAWER_ITEM_POSTS : NAVDRAWER_ITEM_INVALID;
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
        data.putInt(PostsActivity.POST_STATUS, position);

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
        EditPostActivity.launch(PostsActivity.this,
                findViewById(R.id.fab), "new", 3);
    }

    public void editPost(String postId, int postStatus) {
        EditPostActivity.launch(PostsActivity.this,
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
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);

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
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        mUsername = settings.getString("user_login", "");
        mToken = settings.getString("user_status", "");
        mRepo = settings.getString("user_repo", "");

    }

    private void login() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();

        PostsDbHelper db = new PostsDbHelper(this);
        db.dropTables();
        db.close();

        Log.d(LOG_TAG, "Launching login");
        LoginActivity.launch(PostsActivity.this, findViewById(R.id.fab));
        finish();

    }

    public static void launch(BaseActivity activity, View transitionView) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, "fab_create");
        Intent intent = new Intent(activity, PostsActivity.class);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
}

