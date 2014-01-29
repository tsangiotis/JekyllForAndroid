package gr.tsagi.jekyllforandroid;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;


/**
 * Created by tsagi on 9/9/13.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PrefsFragment extends PreferenceFragment {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}