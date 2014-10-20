package gr.tsagi.jekyllforandroid.app.activities;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import gr.tsagi.jekyllforandroid.app.R;
import gr.tsagi.jekyllforandroid.app.fragments.EditPostFragment;

@SuppressLint({"DefaultLocale", "SimpleDateFormat"})
public class EditPostActivity extends BaseActivity {

    private static final String LOG_TAG = EditPostActivity.class.getSimpleName();

    public static final String POST_ID = "post_id";
    public static final String POST_STATUS = "post_status";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // Set color
            tintManager.setTintColor(getResources().getColor(R.color.primary));
        }

        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
        String postId = getIntent().getStringExtra(POST_ID);
        int postStatus = getIntent().getIntExtra(POST_STATUS, -1);

        Bundle arguments = new Bundle();
        arguments.putString(EditPostActivity.POST_ID, postId);
        arguments.putInt(EditPostActivity.POST_STATUS, postStatus);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment = new EditPostFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();

    }

    @Override protected int getLayoutResource() {
        return R.layout.activity_edit_post;
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