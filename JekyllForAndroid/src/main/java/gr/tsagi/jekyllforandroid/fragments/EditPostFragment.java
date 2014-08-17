package gr.tsagi.jekyllforandroid.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.yaml.snakeyaml.Yaml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.activities.EditPostActivity;
import gr.tsagi.jekyllforandroid.activities.PostsListActivity;
import gr.tsagi.jekyllforandroid.activities.PreviewMarkdownActivity;
import gr.tsagi.jekyllforandroid.data.PostsContract.CategoryEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.PostEntry;
import gr.tsagi.jekyllforandroid.data.PostsContract.TagEntry;
import gr.tsagi.jekyllforandroid.github.GithubPush;

public class EditPostFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditPostFragment.class.getSimpleName();

    private static final int EDIT_POST_LOADER = 0;

    private static final String[] POST_COLUMNS = {
            PostEntry.COLUMN_POST_ID,
            PostEntry.COLUMN_TITLE,
            PostEntry.COLUMN_CONTENT,
            TagEntry.COLUMN_TAG,
            CategoryEntry.COLUMN_CATEGORY
    };

    private String mPostId;
    private int mPostStatus;

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
            mPostStatus = getArguments().getInt(EditPostActivity.POST_STATUS);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.post, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_publish:
                publishPost();
                return true;
            case R.id.action_draft:
                uploadDraft();
                return true;
            case android.R.id.home:
                startActivity(new Intent(getActivity(), PostsListActivity.class));
                return true;
            case R.id.action_preview:
                previewMarkdown();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        // Sort order:  Descending, by date.

        Log.d(LOG_TAG, "postId: " + mPostId);

        Uri postFromId;
        if(mPostStatus == 1)
            postFromId = PostEntry.buildPostFromId("draft", mPostId);
        else
            postFromId = PostEntry.buildPostFromId("published", mPostId);

        Log.d(LOG_TAG, "postIdUri: " + postFromId.toString());

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                postFromId,
                POST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read title from cursor
            String title = data.getString(data.getColumnIndex(PostEntry.COLUMN_TITLE));
            mTitle.setText(title);

            String tags = data.getString(data.getColumnIndex(TagEntry.COLUMN_TAG));
            Log.d(LOG_TAG, "tags:" + tags);
            if(!tags.equals("null"))
                mTags.setText(tags);

            String category = data.getString(data.getColumnIndex(CategoryEntry.COLUMN_CATEGORY));
            Log.d(LOG_TAG, "category" + category);
            if(!category.equals("null"))
                mCategory.setText(category);

            String content = data.getString(data.getColumnIndex(PostEntry.COLUMN_CONTENT));
            mContent.setText(content);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private void uploadDraft() {

        final String title = mTitle.getText().toString().trim();
        final String tags = mTags.getText().toString().trim();
        final String category = mCategory.getText().toString().trim();
        final String content = mContent.getText().toString().trim();

        if (content.isEmpty())
            Toast.makeText(getActivity(), R.string.editpost_empty, Toast.LENGTH_LONG).show();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_confirm_draft);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    pushDraft(title, tags, category, content);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    public void pushDraft(String title, String tags, String category, String content) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Map<String, Object> data = new HashMap<String, Object>();

        Yaml yaml = new Yaml();
        String customYaml = prefs.getString("yaml_values", "");
        Log.d(LOG_TAG, customYaml);
        Map<String, Object> map = (HashMap<String, Object>) yaml.load(customYaml);

        data.put("title", title);
        data.put("tags", tags.split(","));
        data.put("category", category);
        data.put("layout", "post");
        if (map != null)
            data.putAll(map);

        String output = "---\n" + yaml.dump(data) + "---\n";

        GithubPush pusher = new GithubPush(getActivity());

        try {
            pusher.pushDraft(title, output + content);
            Log.d(LOG_TAG, title + " " + content);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pushPost(String title, String tags, String category, String content) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Map<String, Object> data = new HashMap<String, Object>();
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Yaml yaml = new Yaml();
        String customYaml = prefs.getString("yaml_values", "");
        Log.d("yaml", customYaml);
        Map<String, Object> map = (HashMap<String, Object>) yaml.load(customYaml);
        data.put("tags", tags.split(","));
        data.put("category", category);
        data.put("title", title);
        data.put("layout", "post");
        if (map != null)
            data.putAll(map);

        String output = "---\n" + yaml.dump(data) + "---\n";

        GithubPush pusher = new GithubPush(getActivity());

        try {
            pusher.pushContent(title, date, output + content);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private void publishPost() {

        final String title = mTitle.getText().toString().trim();
        final String tags = mTags.getText().toString().trim();
        final String category = mCategory.getText().toString().trim();
        final String content = mContent.getText().toString().trim();

        if (content.isEmpty())
            Toast.makeText(getActivity(), R.string.editpost_empty, Toast.LENGTH_LONG).show();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_confirm_update);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    pushPost(title, tags, category, content);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    public void previewMarkdown() {

        final String content = mContent.getText().toString().trim();

        if (!content.isEmpty()) {
            Intent myIntent = new Intent(getActivity(), PreviewMarkdownActivity.class);
            myIntent.putExtra("content", content);
            startActivity(myIntent);
        } else
            Toast.makeText(getActivity(), "Nothing to preview", Toast.LENGTH_SHORT).show();
    }

}
