package gr.tsagi.jekyllforandroid.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import gr.tsagi.jekyllforandroid.app.R;

public class PreviewMarkdownActivity extends BaseActivity {

    public static final String POST_CONTENT = "content";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
