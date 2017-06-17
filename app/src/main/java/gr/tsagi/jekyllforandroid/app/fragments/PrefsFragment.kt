package gr.tsagi.jekyllforandroid.app.fragments

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceFragment

import app.wt.noolis.R

/**
\* Created with IntelliJ IDEA.
\* User: tsagi
\* Date: 9/9/13
\* Time: 9:15
\*/
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class PrefsFragment : PreferenceFragment() {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences)
    }
}