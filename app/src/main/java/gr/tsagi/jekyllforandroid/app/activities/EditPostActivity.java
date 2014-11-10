package gr.tsagi.jekyllforandroid.app.activities;

import android.app.AlertDialog;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.yaml.snakeyaml.Yaml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import gr.tsagi.jekyllforandroid.app.R;
import gr.tsagi.jekyllforandroid.app.data.PostsContract;
import gr.tsagi.jekyllforandroid.app.utils.GithubPush;
import gr.tsagi.jekyllforandroid.app.utils.Utility;

public class EditPostActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = EditPostActivity.class.getSimpleName();

    private static final int EDIT_POST_LOADER = 0;

    private static final String[] POST_COLUMNS = {
            PostsContract.PostEntry.COLUMN_POST_ID,
            PostsContract.PostEntry.COLUMN_TITLE,
            PostsContract.PostEntry.COLUMN_CONTENT,
            PostsContract.PostEntry.COLUMN_TAGS,
            PostsContract.PostEntry.COLUMN_CATEGORY
    };

    private String mPostId;
    private int mPostStatus;

    private EditText mTitle;
    private EditText mTags;
    private EditText mCategory;
    private EditText mContent;

    ImageButton publish;

    Utility utility;

    public static final String POST_ID = "post_id";
    public static final String POST_STATUS = "post_status";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
        mPostId = getIntent().getStringExtra(POST_ID);
        mPostStatus = getIntent().getIntExtra(POST_STATUS, -1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        utility = new Utility(this);

        mTitle = (EditText) findViewById(R.id.edit_title);
        mTags = (EditText) findViewById(R.id.edit_tags);
        mCategory = (EditText) findViewById(R.id.edit_category);
        mContent = (EditText) findViewById(R.id.edit_content);

        publish = (ImageButton) findViewById(R.id.fab);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                publishPost();
            }
        });

    }

    @Override protected int getLayoutResource() {
        return R.layout.activity_edit_post;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        // if it is present.
        getMenuInflater().inflate(R.menu.post, menu);

        // Just for the logout
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_draft:
                uploadDraft();
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
        if(mPostStatus == 1) {
            postFromId = PostsContract.PostEntry.buildPostFromId("draft", mPostId);
            setActionBarTitle(getResources().getString(R.string.edit_draft));
        } else {
            postFromId = PostsContract.PostEntry.buildPostFromId("published", mPostId);
            setActionBarTitle(getResources().getString(R.string.edit_post));
        }

        Log.d(LOG_TAG, "postIdUri: " + postFromId.toString());

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                this,
                postFromId,
                POST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read title from cursor
            String title = data.getString(data.getColumnIndex(PostsContract.PostEntry.COLUMN_TITLE));
            mTitle.setText(title);

            String tags = data.getString(data.getColumnIndex(PostsContract.PostEntry.COLUMN_TAGS));
            Log.d(LOG_TAG, "tags:" + tags);
            if(!tags.equals("null"))
                mTags.setText(tags);

            String category = data.getString(data.getColumnIndex(PostsContract.PostEntry
                    .COLUMN_CATEGORY));
            Log.d(LOG_TAG, "category: " + category);
            if(!category.equals("null"))
                mCategory.setText(category);

            String content = data.getString(data.getColumnIndex(PostsContract.PostEntry.COLUMN_CONTENT));
            mContent.setText(content);
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    private void uploadDraft() {

        final String title = mTitle.getText().toString().trim();
        final String tags = mTags.getText().toString().trim();
        final String category = mCategory.getText().toString().trim();
        final String content = mContent.getText().toString().trim();

        if (content.equals(""))
            Toast.makeText(this, R.string.editpost_empty, Toast.LENGTH_LONG).show();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

        GithubPush pusher = new GithubPush(this);

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

        GithubPush pusher = new GithubPush(this);

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

        if (content.equals(""))
            Toast.makeText(this, R.string.editpost_empty, Toast.LENGTH_LONG).show();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        final String repo = utility.getRepo();

        if (!content.equals("")) {
            Intent myIntent = new Intent(this, PreviewMarkdownActivity.class);
            myIntent.putExtra(PreviewMarkdownActivity.POST_CONTENT, content);
            startActivity(myIntent);
        } else
            Toast.makeText(this, "Nothing to preview", Toast.LENGTH_SHORT).show();
    }

    public static void launch(BaseActivity activity, View transitionView, String postId,
                              int postStatus) {

        Intent intent = new Intent(activity, EditPostActivity.class);
        if (postId.equals("new") && postStatus == 3) {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity, transitionView, "fab_publish");
            ActivityCompat.startActivity(activity, intent, options.toBundle());
        }
        else {
            intent.putExtra(EditPostActivity.POST_ID, postId);
            intent.putExtra(EditPostActivity.POST_STATUS, postStatus);
            activity.startActivity(intent);
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}