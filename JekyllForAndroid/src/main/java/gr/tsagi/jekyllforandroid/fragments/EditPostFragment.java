package gr.tsagi.jekyllforandroid.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.activities.EditPostActivity;
import gr.tsagi.jekyllforandroid.data.PostsContract.CategoryEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.TagEntry;

public class EditPostFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditPostFragment.class.getSimpleName();

    private static final int EDIT_POST_LOADER = 0;

    private static final String[] POST_COLUMNS = {
            PostEntry.COLUMN_POST_ID,
            PostEntry.COLUMN_TITLE,
            TagEntry.COLUMN_TAG,
            CategoryEntry.COLUMN_CATEGORY
    };

    private String mPostId;

    private EditText mTitle;
    private EditText mTags;
    private EditText mCategory;
    private EditText mContent;

    public EditPostFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPostId = getArguments().getString(EditPostActivity.POST_ID);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_post, container, false);

        mTitle = (EditText) rootView.findViewById(R.id.edit_title);
        mTags = (EditText) rootView.findViewById(R.id.edit_tags);
        mCategory = (EditText) rootView.findViewById(R.id.edit_category);
        mContent = (EditText) rootView.findViewById(R.id.edit_content);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPostId != null) {
            getLoaderManager().restartLoader(EDIT_POST_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = PostEntry.COLUMN_DATETEXT + " DESC";

        Uri postFromId = PostEntry.buildPostFromId(mPostId);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                postFromId,
                POST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
            String postId = data.getString(data.getColumnIndex(PostEntry.COLUMN_POST_ID));
            // Use weather art image


            // Read date from cursor and update views for day of week and date
            String title = data.getString(data.getColumnIndex(PostEntry.COLUMN_TITLE));
            String whereTag = TagEntry.COLUMN_POST_ID + " =?";
            mTitle.setText(postId);

            String content = data.getString(data.getColumnIndex(PostEntry.COLUMN_CONTENT));
            mContent.setText(content);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}
