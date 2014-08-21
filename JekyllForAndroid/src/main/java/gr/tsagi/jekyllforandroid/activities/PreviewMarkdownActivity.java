package gr.tsagi.jekyllforandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.fragments.MarkdownPreviewFragment;

public class PreviewMarkdownActivity extends ActionBarActivity {

    public static final String POST_CONTENT = "content";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_preview);

        Intent myIntent = getIntent();
        String content = myIntent.getStringExtra(POST_CONTENT);


        Bundle arguments = new Bundle();
        arguments.putString(POST_CONTENT, content);

        Fragment fragment = new MarkdownPreviewFragment();
        fragment.setArguments(arguments);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.markdown_preview_container, fragment)
                .commit();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
