package gr.tsagi.jekyllforandroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.commonsware.cwac.anddown.AndDown;

import gr.tsagi.jekyllforandroid.utils.ImmersiveView;
import gr.tsagi.jekyllforandroid.utils.TranslucentBars;

public class PreviewMarkdownActivity extends Activity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        new TranslucentBars(this).tint(false);
        new ImmersiveView(this);

        WebView webview = new WebView(this);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.setWebChromeClient(new WebChromeClient());
        setContentView(webview);

        Intent myIntent = getIntent();
        String mContent = myIntent.getStringExtra("content");
        String repo = "http://" + myIntent.getStringExtra("repo");

        String content = mContent.replace("{{ site.url }}", repo);

        AndDown andDown = new AndDown();
        String htmlData = andDown.markdownToHtml(content);

        htmlData = "<meta name=\"HandheldFriendly\" content=\"True\">\n" +
                "<meta name=\"MobileOptimized\" content=\"320\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"+
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/main.min.css?family=Lato:300,400,700,300italic,400italic\" />" +
                "<meta http-equiv=\"cleartype\" content=\"on\">"+
                "<script src=\"js/vendor/modernizr-2.6.2.custom.min.js\"></script>"+
                htmlData;

        webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
    }
}
