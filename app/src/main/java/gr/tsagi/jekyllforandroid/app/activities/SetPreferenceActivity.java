package gr.tsagi.jekyllforandroid.app.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.app.fragments.PrefsFragment;

/**
 * Created by tsagi on 9/9/13.
 */
public class SetPreferenceActivity extends ActionBarActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // Set color
        tintManager.setTintColor(getResources().getColor(R.color.actionbar_bg));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){

            getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
        }
        else{
            Intent set = new Intent(this, SettingsActivity.class);
            startActivity(set);
        }
    }
}