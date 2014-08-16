package gr.tsagi.jekyllforandroid.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.yaml.snakeyaml.Yaml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.fragments.EditPostFragment;
import gr.tsagi.jekyllforandroid.github.GithubPush;

@SuppressLint({"DefaultLocale", "SimpleDateFormat"})
public class EditPostActivity extends Activity {

    private static final String LOG_TAG = EditPostActivity.class.getSimpleName();

    public static final String POST_ID = "post_id";

    String mToken;
    String mDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String mTitle;
    String mCategory;
    String mTags;
    String mContent;

    String message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            String postId  = getIntent().getStringExtra(POST_ID);

            Log.d(LOG_TAG, "post_id: " + postId);

            Bundle arguments = new Bundle();
            arguments.putString(EditPostActivity.POST_ID, postId);

            EditPostFragment fragment = new EditPostFragment();
            fragment.setArguments(arguments);

            getFragmentManager().beginTransaction()
                    .add(R.id.edit_post_container, fragment)
                    .commit();


        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        /**
         * Restore draft if any is available
         */

        if (mToken == "") {
            Toast.makeText(EditPostActivity.this, "Please login", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
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
                startActivity(new Intent(EditPostActivity.this, PostsListActivity.class));
                return true;
            case R.id.action_preview:
                previewMarkdown();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void uploadDraft() {

        final EditText content = (EditText) findViewById(R.id.editTextContent);

        if (content.getText().toString().isEmpty())
            Toast.makeText(EditPostActivity.this, R.string.editpost_empty, Toast.LENGTH_LONG).show();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_confirm_draft);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    pushDraft();
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

    public void pushPost() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, Object> data = new HashMap<String, Object>();

        Yaml yaml = new Yaml();
        String customYaml = prefs.getString("yaml_values", "");
        Log.d("yaml", customYaml);
        Map<String, Object> map = (HashMap<String, Object>) yaml.load(customYaml);
        data.put("tags", mTags.split(","));
        data.put("category", mCategory);
        data.put("title", mTitle);
        data.put("layout", "post");
        if (map != null)
            data.putAll(map);

        String output = "---\n" + yaml.dump(data) + "---\n";

        GithubPush pusher = new GithubPush(EditPostActivity.this);

        try {
            pusher.pushContent(mTitle, mDate, output + mContent);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pushDraft() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, Object> data = new HashMap<String, Object>();

        Yaml yaml = new Yaml();
        String customYaml = prefs.getString("yaml_values", "");
        Log.d("yaml", customYaml);
        Map<String, Object> map = (HashMap<String, Object>) yaml.load(customYaml);
        data.put("tags", mTags.split(","));
        data.put("category", mCategory);
        data.put("title", mTitle);
        data.put("layout", "post");
        if (map != null)
            data.putAll(map);

        String output = "---\n" + yaml.dump(data) + "---\n";

        GithubPush pusher = new GithubPush(EditPostActivity.this);

        try {
            pusher.pushDraft(mTitle, output + mContent);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pushResult(String result) {
        String message;
        if (result.equals("OK")) {
            message = getString(R.string.editpost_publish);
        } else
            message = getString(R.string.editpost_fail);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void publishPost() {

        final EditText content = (EditText) findViewById(R.id.editTextContent);

        if (content.getText().toString().isEmpty())
            Toast.makeText(EditPostActivity.this, R.string.editpost_empty, Toast.LENGTH_LONG).show();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_confirm_update);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    pushPost();
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
        if (!mContent.isEmpty()) {
            Intent myIntent = new Intent(EditPostActivity.this, PreviewMarkdownActivity.class);
            myIntent.putExtra("content", mContent);
            startActivity(myIntent);
        } else
            Toast.makeText(this, "Nothing to preview", Toast.LENGTH_SHORT).show();
    }

}