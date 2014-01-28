package gr.tsagi.jekyllforandroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by tsagi on 9/10/13.
 */
public class SettingsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.preferences);
            }
}