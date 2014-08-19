package gr.tsagi.jekyllforandroid.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.fragments.EditPostFragment;

@SuppressLint({"DefaultLocale", "SimpleDateFormat"})
public class EditPostActivity extends Activity {

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

        EditPostFragment fragment = new EditPostFragment();
        fragment.setArguments(arguments);

        getFragmentManager().beginTransaction()
                .add(R.id.edit_post_container, fragment)
                .commit();

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

    }

}