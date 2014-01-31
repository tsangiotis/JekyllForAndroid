package gr.tsagi.jekyllforandroid.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import gr.tsagi.jekyllforandroid.R;

/**
 * Created by tsagi on 9/10/13.
 */
public class SettingsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.preferences);
            }
}