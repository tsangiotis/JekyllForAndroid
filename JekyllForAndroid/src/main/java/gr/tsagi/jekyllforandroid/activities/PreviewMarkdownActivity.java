package gr.tsagi.jekyllforandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.fragments.MarkdownPreviewFragment;

public class PreviewMarkdownActivity extends ActionBarActivity {

    public static final String POST_CONTENT = "content";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_preview);

        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // Set color
        tintManager.setTintColor(getResources().getColor(R.color.actionbar_bg));

        Intent myIntent = getIntent();
        String content = myIntent.getStringExtra(POST_CONTENT);

        Bundle arguments = new Bundle();
        arguments.putString(POST_CONTENT, content);

        Fragment fragment = new MarkdownPreviewFragment();
        fragment.setArguments(arguments);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.markdown_preview_container, fragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
