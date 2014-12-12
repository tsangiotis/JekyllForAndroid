package gr.tsagi.jekyllforandroid.app.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import java.util.regex.Pattern;

import gr.tsagi.jekyllforandroid.app.BuildConfig;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGE;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGI;

/**
 * Created by tsagi on 12/12/14.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = makeLogTag(SyncAdapter.class);

    private static final Pattern sSanitizeAccountNamePattern = Pattern.compile("(.).*?(.?)@");
    public static final String EXTRA_SYNC_USER_DATA_ONLY = "com.google.samples.apps.iosched.EXTRA_SYNC_USER_DATA_ONLY";;

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;

        //noinspection ConstantConditions,PointlessBooleanExpression
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    LOGE(TAG, "Uncaught sync exception, suppressing UI in release build.",
                            throwable);
                }
            });
        }
    }

    @Override
    public void onPerformSync(final Account account, Bundle extras, String authority,
                              final ContentProviderClient provider, final SyncResult syncResult) {
        final boolean uploadOnly = extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        final boolean initialize = extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false);
        final boolean userDataOnly = extras.getBoolean(EXTRA_SYNC_USER_DATA_ONLY, false);

        final String logSanitizedAccountName = sSanitizeAccountNamePattern
                .matcher(account.name).replaceAll("$1...$2@");

        if (uploadOnly) {
            return;
        }

        LOGI(TAG, "Beginning sync for account " + logSanitizedAccountName + "," +
                " uploadOnly=" + uploadOnly +
                " manualSync=" + manualSync +
                " userDataOnly =" + userDataOnly +
                " initialize=" + initialize);

        // Sync from bootstrap and remote data, as needed
        new SyncHelper(mContext).performSync(syncResult, account, extras);
    }

}
