package gr.tsagi.jekyllforandroid.app.activities

//import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import gr.tsagi.jekyllforandroid.app.R
import gr.tsagi.jekyllforandroid.app.fragments.MarkdownPreviewFragment

open class PreviewMarkdownActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // create our manager instance after the content view is set
            //            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            //            tintManager.setStatusBarTintEnabled(true);
            // Set color
            //            tintManager.setTintColor(getResources().getColor(R.color.primary));
        }

        val myIntent = intent
        val content = myIntent.getStringExtra(POST_CONTENT)

        val arguments = Bundle()
        arguments.putString(POST_CONTENT, content)

        val fragment = MarkdownPreviewFragment()
        fragment.arguments = arguments

        //        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        supportFragmentManager.beginTransaction()
                .add(R.id.markdown_preview_container, fragment)
                .commit()

    }

    override val layoutResource: Int
        get() = R.layout.activity_markdown_preview

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        val POST_CONTENT = "content"
    }

}
