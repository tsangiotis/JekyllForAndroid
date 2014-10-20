package gr.tsagi.jekyllforandroid.app.activities;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import gr.tsagi.jekyllforandroid.app.R;

/**
 * Created by tsagi on 9/10/13.
 */
public class SettingsActivity extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // Set color
            tintManager.setTintColor(getResources().getColor(R.color.primary));
        }

        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.preferences);

    }


}