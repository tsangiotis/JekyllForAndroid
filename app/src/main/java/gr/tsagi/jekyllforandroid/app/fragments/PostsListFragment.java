package gr.tsagi.jekyllforandroid.app.fragments;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.app.activities.PostsListActivity;
import gr.tsagi.jekyllforandroid.app.adapters.PostListAdapter;
import gr.tsagi.jekyllforandroid.app.data.PostsContract.PostEntry;


/**
 * Created by tsagi on 7/5/14.
 */
public class PostsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = PostsListFragment.class.getSimpleName();

    private PostListAdapter mPostListAdapter;

    private int status = -1;

    private ActionMode mActionMode;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    private String postid;
    private int pstatus;
    private String content;

    private static final String SELECTED_KEY = "selected_position";

    private static final int LIST_LOADER = 0;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] POSTS_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            PostEntry.TABLE_NAME + "." + PostEntry._ID,
            PostEntry.COLUMN_POST_ID,
            PostEntry.COLUMN_TITLE,
            PostEntry.COLUMN_DATETEXT,
            PostEntry.COLUMN_CONTENT,
            PostEntry.COLUMN_DRAFT
    };


    // These indices are tied to POSTS_COLUMNS. If POSTS_COLUMNS changes, these must change.
    public static final int COL_ID = 0;
    public static final int COL_POST_ID = 1;
    public static final int COL_POST_TITLE = 2;
    public static final int COL_POST_DATE = 3;
    public static final int COL_POST_CONTENT = 4;
    public static final int COL_POST_DRAFT = 5;

    public PostsListFragment() {
        // Empty constructor required for fragment subclasses
        setHasOptionsMenu(true);
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    ((Callback) getActivity())
                            .onItemEditSelected(postid, content, pstatus);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_delete:
                    ((Callback) getActivity())
                            .onItemDeleteSelected(postid, content, pstatus);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    public interface Callback {
        /**
         * PreviewCallback for when an item has been selected.
         */
        public void onItemSelected(String postId, String content, int postStatus);

        public void onItemEditSelected(String postId, String content, int postStatus);

        public void onItemDeleteSelected(String postId, String content, int postStatus);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            status = arguments.getInt(PostsListActivity.POST_STATUS);
        }
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mPostListAdapter = new PostListAdapter(getActivity(), null, 0);

        // Set the first position as the default
        mPosition = 0;

        View rootView = inflater.inflate(R.layout.fragment_posts_list,
                container, false);
        mListView = (ListView) rootView.findViewById(R.id.listview_postslist);
        mListView.setAdapter(mPostListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mPostListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String postid = cursor.getString(COL_POST_ID);
                    int pstatus = cursor.getInt(COL_POST_DRAFT);
                    String content = cursor.getString(COL_POST_CONTENT);
                    ((Callback) getActivity())
                            .onItemSelected(postid, content, pstatus);

                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                                           long l) {
                if (mActionMode != null) {
                    return false;
                }

                view.setSelected(true);

                Cursor cursor = mPostListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    postid = cursor.getString(COL_POST_ID);
                    pstatus = cursor.getInt(COL_POST_DRAFT);
                    content = cursor.getString(COL_POST_CONTENT);

                    // Start the CAB using the ActionMode.Callback defined above
                    ActionBarActivity activity = (ActionBarActivity) getActivity();
                    mActionMode = activity.startSupportActionMode(mActionModeCallback);
                    view.setSelected(true);
                    return true;
                }
                return false;
            }
        });


        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.post_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
//            case R.id.drop_tables:
//                PostsDbHelper db = new PostsDbHelper(getActivity());
//                db.dropTables();
//                db.close();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LIST_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.

        // Sort order:  Descending, by date.
        String sortOrder = PostEntry.COLUMN_POST_ID + " DESC";
        Uri postsUri;

        if (status == 0)
            postsUri = PostEntry.buildPublishedPosts();
        else
            postsUri = PostEntry.buildDraftPosts();

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                postsUri,
                POSTS_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPostListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                mListView.smoothScrollToPosition(mPosition);
        }

        if( mPostListAdapter.getCount() == 0 ) {
            Toast.makeText(getActivity(), "You have nothing to display here. :)",
                    Toast.LENGTH_SHORT).show();
        } else {
            // select the first post and render it.
            if (PostsListActivity.mTwoPane) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mPostListAdapter.notifyDataSetChanged();
                        mListView.performItemClick(
                                mListView.getChildAt(0),
                                0,
                                mListView.getAdapter().getItemId(mListView.getAdapter().getCount()));
                    }
                });

            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPostListAdapter.swapCursor(null);
    }

}
