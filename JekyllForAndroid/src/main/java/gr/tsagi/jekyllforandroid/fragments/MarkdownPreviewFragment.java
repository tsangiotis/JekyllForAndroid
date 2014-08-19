package gr.tsagi.jekyllforandroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewFragment;

import com.commonsware.cwac.anddown.AndDown;

import gr.tsagi.jekyllforandroid.activities.PreviewMarkdownActivity;
import gr.tsagi.jekyllforandroid.utils.Utility;

public class MarkdownPreviewFragment extends WebViewFragment {

    private static final String LOG_TAG = MarkdownPreviewFragment.class.getSimpleName();

    private String content = "";
    private String repo = Utility.getRepo(getActivity());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            content = getArguments().getString(PreviewMarkdownActivity.POST_CONTENT).replace("{{ " +
                    "site.url }}", repo);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        WebView webview = getWebView();
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.setWebChromeClient(new WebChromeClient());

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

        return null;
    }


}