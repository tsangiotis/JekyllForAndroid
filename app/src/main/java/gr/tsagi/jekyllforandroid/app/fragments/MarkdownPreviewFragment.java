package gr.tsagi.jekyllforandroid.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.commonsware.cwac.anddown.AndDown;

import gr.tsagi.jekyllforandroid.app.R;
import gr.tsagi.jekyllforandroid.app.activities.PreviewMarkdownActivity;
import gr.tsagi.jekyllforandroid.app.utils.Utility;

public class MarkdownPreviewFragment extends Fragment {

    private static final String LOG_TAG = MarkdownPreviewFragment.class.getSimpleName();

    private String content = "";

    Utility utility;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utility = new Utility(getActivity());

        final String repo = utility.getRepo();

        if (getArguments() != null) {
            content = getArguments().getString(PreviewMarkdownActivity.POST_CONTENT).replace("{{ " +
                    "site.url }}", repo);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater
                .inflate(R.layout.fragment_markdown_preview, container, false);

        // Find the webview
        WebView webview = (WebView) rootView.findViewById(R.id.markdown_preview_view);

        WebSettings settings = webview.getSettings();
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

        return rootView;
    }


}