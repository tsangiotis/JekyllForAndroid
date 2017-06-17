package gr.tsagi.jekyllforandroid.app.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import app.wt.noolis.R
import com.commonsware.cwac.anddown.AndDown
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

        // Find the webview
        val webview = rootView.findViewById<View>(R.id.markdown_preview_view) as WebView

        val settings = webview.settings
        webview.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort = true
        webview.webChromeClient = WebChromeClient()

        val andDown = AndDown()
        var htmlData = andDown.markdownToHtml(content)

        htmlData = "<meta name=\"HandheldFriendly\" content=\"True\">\n" +
                "<meta name=\"MobileOptimized\" content=\"320\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/main.min.css?family=Lato:300,400,700,300italic,400italic\" />" +
                "<meta http-equiv=\"cleartype\" content=\"on\">" +
                "<script src=\"js/vendor/modernizr-2.6.2.custom.min.js\"></script>" +
                htmlData

        webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null)

        return rootView
    }

    companion object {

        private val LOG_TAG = MarkdownPreviewFragment::class.java.simpleName
    }


}