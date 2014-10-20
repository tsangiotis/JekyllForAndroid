package gr.tsagi.jekyllforandroid.app.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import gr.tsagi.jekyllforandroid.app.R;

/**
 * Created by tsagi on 10/20/14.
 */

public abstract class BaseActivity extends ActionBarActivity {

    private Toolbar toolbar;

    private String LOG_TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getLayoutResource() != 0) {
            setContentView(getLayoutResource());
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            Log.d(LOG_TAG, "To be created");
            if (toolbar != null) {
                Log.d(LOG_TAG, "Created");
                setSupportActionBar(toolbar);
//            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            }
        }

    }

    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }

    protected void setActionBarTitle(CharSequence title){
        toolbar.setTitle(title);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }
}