package gr.tsagi.jekyllforandroid.app.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.tiagohm.markdownview.MarkdownView
import br.tiagohm.markdownview.css.styles.Github
import com.jchanghong.utils.removeyam
import gr.tsagi.jekyllforandroid.app.R
import gr.tsagi.jekyllforandroid.app.activities.PreviewMarkdownActivity
import gr.tsagi.jekyllforandroid.app.utils.Utility

class MarkdownPreviewFragment : Fragment() {

    private var content = ""

    lateinit internal var utility: Utility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        utility = Utility(activity)

        val repo = utility.repo

        if (arguments != null) {
            content = arguments.getString(PreviewMarkdownActivity.POST_CONTENT)!!.replace("{{ " + "site.url }}", repo)
        }


    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater!!
                .inflate(R.layout.fragment_markdown_preview, container, false)
        val mMarkdownView: MarkdownView
        mMarkdownView = rootView.findViewById<MarkdownView>(R.id.markdown_preview_view)
        mMarkdownView.loadMarkdown(content.removeyam())
        val css = Github()
        //        css.addFontFace("MyFont", "condensed", "italic", "bold", "url('myfont.ttf')");
        //        css.addMedia("screen and (min-width: 1281px)");
        css.addRule("h1", "color: black")
        css.endMedia()
        css.addRule("h1", "color: green")
        css.addRule("h2", "color: blue")
        css.addRule("h3", "color: green")
        css.addRule("h4", "color: blue")
        mMarkdownView.addStyleSheet(css)
        //        mMarkdownView.loadMarkdownFromAsset("markdown1.md");
        //        mMarkdownView.loadMarkdownFromFile(new File());
        //        mMarkdownView.loadMarkdownFromUrl("url");
        //        // Find the webview
        //        WebView webview = (WebView) rootView.findViewById(R.id.markdown_preview_view);
        //
        //        WebSettings settings = webview.getSettings();
        //        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        //        webview.getSettings().setLoadWithOverviewMode(true);
        //        webview.getSettings().setUseWideViewPort(true);
        //        webview.setWebChromeClient(new WebChromeClient());
        //
        //        AndDown andDown = new AndDown();
        //        String htmlData = andDown.markdownToHtml(content);
        //
        //        htmlData = "<meta name=\"HandheldFriendly\" content=\"True\">\n" +
        //                "<meta name=\"MobileOptimized\" content=\"320\">\n" +
        //                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"+
        //                "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/main.min.css?family=Lato:300,400,700,300italic,400italic\" />" +
        //                "<meta http-equiv=\"cleartype\" content=\"on\">"+
        //                "<script src=\"js/vendor/modernizr-2.6.2.custom.min.js\"></script>"+
        //                htmlData;
        //
        //        webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);

        return rootView
    }

    companion object {

        private val LOG_TAG = MarkdownPreviewFragment::class.java.simpleName
    }


}