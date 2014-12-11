package gr.tsagi.jekyllforandroid.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import gr.tsagi.jekyllforandroid.app.R;
import gr.tsagi.jekyllforandroid.app.utils.AnalyticsManager;

import static gr.tsagi.jekyllforandroid.app.utils.LogUtils.makeLogTag;

/**
 * Created by tsagi on 9/9/13.
 */

public class PostsActivity extends BaseActivity implements PostsListFragment.Callback {

    private static final String TAG = makeLogTag(PostsActivity.class);

    // How is this Activity being used?
    private static final int MODE_POST = 0; // as top-level "Post" screen

    private static final String STATE_FILTER_0 = "STATE_FILTER_0";
    private static final String STATE_FILTER_1 = "STATE_FILTER_1";
    private static final String STATE_FILTER_2 = "STATE_FILTER_2";

    public static final String EXTRA_FILTER_TAG = "gr.tsagi.jekyllforandroid.app.extra.FILTER_TAG";

    private int mMode = MODE_POST;

    private final static String SCREEN_LABEL = "Posts";

    // filter tags that are currently selected
    private String[] mFilterTags = { "", "", "" };

    // filter tags that we have to restore (as a result of Activity recreation)
    private String[] mFilterTagsToRestore = { null, null, null };

    private PostsListFragment mPostsFrag = null;

    public static final String POST_STATUS = "post_status";

    private View mButterBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_posts_list);

        Toolbar toolbar = getActionBarToolbar();

        AnalyticsManager.sendScreenView(SCREEN_LABEL);

        overridePendingTransition(0, 0);

        if (savedInstanceState != null) {
            mFilterTagsToRestore[0] = mFilterTags[0] = savedInstanceState.getString(STATE_FILTER_0);
            mFilterTagsToRestore[1] = mFilterTags[1] = savedInstanceState.getString(STATE_FILTER_1);
            mFilterTagsToRestore[2] = mFilterTags[2] = savedInstanceState.getString(STATE_FILTER_2);
        } else if (getIntent() != null && getIntent().hasExtra(EXTRA_FILTER_TAG)) {
            mFilterTagsToRestore[0] = getIntent().getStringExtra(EXTRA_FILTER_TAG);
        }

        toolbar.setTitle(SCREEN_LABEL);

        mButterBar = findViewById(R.id.butter_bar);
        registerHideableHeaderView(mButterBar);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mPostsFrag = (PostsListFragment) getFragmentManager().findFragmentById(
                R.id.sessions_fragment);
        if (mPostsFrag != null && savedInstanceState == null) {
            Bundle args = intentToFragmentArguments(getIntent());
            mPostsFrag.reloadFromArguments(args);
        }
    }

    public static void launch(BaseActivity activity, View transitionView) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, "fab_create");
        Intent intent = new Intent(activity, PostsActivity.class);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    public void onItemSelected(String postId, String content, int postStatus) {

    }

    @Override
    public void onItemEditSelected(String postId, String content, int postStatus) {

    }

    @Override
    public void onItemDeleteSelected(String postId, String content, int postStatus) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}

