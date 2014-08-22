package gr.tsagi.jekyllforandroid.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.fragments.EditPostFragment;

@SuppressLint({"DefaultLocale", "SimpleDateFormat"})
public class EditPostActivity extends ActionBarActivity {

    private static final String LOG_TAG = EditPostActivity.class.getSimpleName();

    public static final String POST_ID = "post_id";
    public static final String POST_STATUS = "post_status";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
        String postId = getIntent().getStringExtra(POST_ID);
        int postStatus = getIntent().getIntExtra(POST_STATUS, -1);

        Bundle arguments = new Bundle();
        arguments.putString(EditPostActivity.POST_ID, postId);
        arguments.putInt(EditPostActivity.POST_STATUS, postStatus);

        Fragment fragment = new EditPostFragment();
        fragment.setArguments(arguments);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.edit_post_container, fragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}