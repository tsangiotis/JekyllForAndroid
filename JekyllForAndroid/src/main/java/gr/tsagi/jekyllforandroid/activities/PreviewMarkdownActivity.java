package gr.tsagi.jekyllforandroid.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.commonsware.cwac.anddown.AndDown;

public class PreviewMarkdownActivity extends Activity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webview = new WebView(this);
        setContentView(webview);

        Intent myIntent = getIntent();
        String mContent = myIntent.getStringExtra("content");
        String repo = "http://" + myIntent.getStringExtra("repo");

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        String content = mContent.replace("{{ site.url }}", repo);

        AndDown andDown = new AndDown();
        String htmlData = andDown.markdownToHtml(content);

        htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + htmlData;

        webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return(true);
        }

        return(super.onOptionsItemSelected(item));
    }

}
