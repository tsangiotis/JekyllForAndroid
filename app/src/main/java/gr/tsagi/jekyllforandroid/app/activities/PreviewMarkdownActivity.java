package gr.tsagi.jekyllforandroid.app.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import app.wt.noolis.R;
import gr.tsagi.jekyllforandroid.app.fragments.MarkdownPreviewFragment;

public class PreviewMarkdownActivity extends BaseActivity {

    public static final String POST_CONTENT = "content";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // Set color
            tintManager.setTintColor(getResources().getColor(R.color.primary));
        }

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

    @Override protected int getLayoutResource() {
        return R.layout.activity_markdown_preview;
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
