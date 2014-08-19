package gr.tsagi.jekyllforandroid.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.fragments.MarkdownPreviewFragment;

public class PreviewMarkdownActivity extends Activity {

    public static final String POST_CONTENT = "content";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_preview);

        Intent myIntent = getIntent();
        String content = myIntent.getStringExtra(POST_CONTENT);


        Bundle arguments = new Bundle();
        arguments.putString(POST_CONTENT, content);

        MarkdownPreviewFragment fragment = new MarkdownPreviewFragment();
        fragment.setArguments(arguments);

        getFragmentManager().beginTransaction()
                .add(R.id.markdown_preview_container, fragment)
                .commit();


        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

}
