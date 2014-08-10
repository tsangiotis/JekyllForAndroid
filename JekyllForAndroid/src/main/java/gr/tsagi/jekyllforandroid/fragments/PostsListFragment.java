package gr.tsagi.jekyllforandroid.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Date;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.adapters.PostListAdapter;
import gr.tsagi.jekyllforandroid.data.PostsContract;
import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;
import gr.tsagi.jekyllforandroid.utils.FetchPostsTask;


/**
 * Created by tsagi on 7/5/14.
 */
public  class PostsListFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private PostListAdapter mPostListAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

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
            PostEntry.COLUMN_TITLE,
            PostEntry.COLUMN_DATETEXT,
    };


    // These indices are tied to POSTS_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_POST_ID = 0;
    public static final int COL_POST_TITLE = 1;
    public static final int COL_POST_DATE = 2;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String date);
    }

    public PostsListFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        updatePosts();
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mPostListAdapter = new PostListAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_posts_list,
                container, false);
        mListView = (ListView) rootView.findViewById(R.id.posts_list);
        mListView.setAdapter(mPostListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                Cursor cursor = mPostListAdapter.getCursor();
//                if (cursor != null && cursor.moveToPosition(position)) {
//                    ((Callback)getActivity())
//                            .onItemSelected(cursor.getString(COL_WEATHER_DATE));
//                }
//                mPosition = position;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updatePosts() {
        new FetchPostsTask(getActivity()).execute();
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
        String startDate = PostsContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = PostEntry.COLUMN_DATETEXT + " DESC";

        Uri weatherForLocationUri = PostEntry.buildPosts();

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                POSTS_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPostListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPostListAdapter.swapCursor(null);
    }

}