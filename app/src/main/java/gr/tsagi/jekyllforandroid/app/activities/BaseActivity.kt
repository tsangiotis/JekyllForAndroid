package gr.tsagi.jekyllforandroid.app.activities

import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.ActionBarActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem

import app.wt.noolis.R

/**
 * Created by tsagi on 10/20/14.
 */

abstract class BaseActivity : ActionBarActivity() {

    private var toolbar: Toolbar? = null

    private val LOG_TAG = BaseActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (layoutResource != 0) {
            setContentView(layoutResource)
            toolbar = findViewById(R.id.toolbar) as Toolbar
            Log.d(LOG_TAG, "To be created")
            if (toolbar != null) {
                Log.d(LOG_TAG, "Created")
                setSupportActionBar(toolbar)
                //            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            }
        }

    }

    protected abstract val layoutResource: Int

    protected fun setActionBarIcon(iconRes: Int) {
        toolbar!!.setNavigationIcon(iconRes)
    }

    protected fun setActionBarTitle(title: CharSequence) {
        toolbar!!.title = title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> NavUtils.navigateUpFromSameTask(this)
        }

        return super.onOptionsItemSelected(item)
    }
}