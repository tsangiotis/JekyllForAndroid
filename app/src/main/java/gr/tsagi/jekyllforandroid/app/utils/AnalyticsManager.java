package gr.tsagi.jekyllforandroid.app.utils;

/**
 * Created by tsagi on 12/6/14.
 */


import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import gr.tsagi.jekyllforandroid.app.BuildConfig;
import gr.tsagi.jekyllforandroid.app.R;

import static gr.tsagi.jekyllforandroid.app.utils.LogUtils.LOGD;

public class AnalyticsManager {
    private static Context sAppContext = null;

    private static Tracker mTracker;
    private final static String TAG = LogUtils.makeLogTag(AnalyticsManager.class);

    public static synchronized void setTracker(Tracker tracker) {
        mTracker = tracker;
    }

    private static boolean canSend() {
        // We can only send Google Analytics when ALL the following conditions are true:
        //    1. This module has been initialized.
        //    2. The user has accepted the ToS.
        //    3. Analytics is enabled in Settings.
        return sAppContext != null && mTracker != null && PrefUtils.isTosAccepted(sAppContext) &&
                PrefUtils.isAnalyticsEnabled(sAppContext);
    }

    public static void sendScreenView(String screenName) {
        if (canSend()) {
            mTracker.setScreenName(screenName);
            mTracker.send(new HitBuilders.AppViewBuilder().build());
            LOGD(TAG, "Screen View recorded: " + screenName);
        } else {
            LOGD(TAG, "Screen View NOT recorded (analytics disabled or not ready).");
        }
    }

    public static void sendEvent(String category, String action, String label, long value) {
        if (canSend()) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(label)
                    .setValue(value)
                    .build());

            LOGD(TAG, "Event recorded:");
            LOGD(TAG, "\tCategory: " + category);
            LOGD(TAG, "\tAction: " + action);
            LOGD(TAG, "\tLabel: " + label);
            LOGD(TAG, "\tValue: " + value);
        } else {
            LOGD(TAG, "Analytics event ignored (analytics disabled or not ready).");
        }
    }

    public static void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, 0);
    }

    public Tracker getTracker() {
        return mTracker;
    }

    public static synchronized void initializeAnalyticsTracker(Context context) {
        sAppContext = context;
        if (mTracker == null) {
            int useProfile;
            if (BuildConfig.DEBUG) {
                LOGD(TAG, "Analytics manager using DEBUG ANALYTICS PROFILE.");
                useProfile = R.xml.analytics_debug;
            } else {
                useProfile = R.xml.analytics_release;
            }
            mTracker = GoogleAnalytics.getInstance(context).newTracker(useProfile);
        }
    }
}
