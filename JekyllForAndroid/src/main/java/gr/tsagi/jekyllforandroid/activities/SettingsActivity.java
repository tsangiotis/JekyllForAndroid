package gr.tsagi.jekyllforandroid.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import gr.tsagi.jekyllforandroid.R;

/**
 * Created by tsagi on 9/10/13.
 */
public class SettingsActivity extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // Set color
        tintManager.setTintColor(getResources().getColor(R.color.actionbar_bg));

        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.preferences);

    }


}