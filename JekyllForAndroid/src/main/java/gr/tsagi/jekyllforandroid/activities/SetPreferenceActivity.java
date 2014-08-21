package gr.tsagi.jekyllforandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import gr.tsagi.jekyllforandroid.fragments.PrefsFragment;

/**
 * Created by tsagi on 9/9/13.
 */
public class SetPreferenceActivity extends ActionBarActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 11){

            getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
        }
        else{
            Intent set = new Intent(this, SettingsActivity.class);
            startActivity(set);
        }
    }
}