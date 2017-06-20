package gr.tsagi.jekyllforandroid.app.activities

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem

import gr.tsagi.jekyllforandroid.app.R

/**
 * Created by tsagi on 10/20/14.
 */

abstract class BaseActivity : AppCompatActivity() {

    private var toolbar: AppBarLayout? = null

    private val LOG_TAG = BaseActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (layoutResource != 0) {
            setContentView(layoutResource)
            toolbar = findViewById(R.id.toolbar) as AppBarLayout
            Log.d(LOG_TAG, "To be created")
            if (toolbar != null) {
                Log.d(LOG_TAG, "Created")
                //                setSupportActionBar(toolbar);
                //            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            }
        }

    }

    protected abstract val layoutResource: Int

    protected fun setActionBarIcon(iconRes: Int) {
        //        toolbar.setNavigationIcon(iconRes);
    }

    protected fun setActionBarTitle(title: CharSequence) {
        //        toolbar.setTitle(title);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> NavUtils.navigateUpFromSameTask(this)
        }

        return super.onOptionsItemSelected(item)
    }
}