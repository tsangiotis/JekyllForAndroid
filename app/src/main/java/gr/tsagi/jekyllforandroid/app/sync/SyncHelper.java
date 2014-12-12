package gr.tsagi.jekyllforandroid.app.sync;

/**
 * Created by tsagi on 12/12/14.
 */

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;

import java.io.IOException;

import gr.tsagi.jekyllforandroid.app.Config;
import gr.tsagi.jekyllforandroid.app.provider.PostsContract;
import gr.tsagi.jekyllforandroid.app.util.PrefUtils;
import gr.tsagi.jekyllforandroid.app.util.UIUtils;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGD;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGE;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGI;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGW;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.makeLogTag;

/**
 * A helper class for dealing with conference data synchronization.
 * All operations occur on the thread they're called from, so it's best to wrap
 * calls in an {@link android.os.AsyncTask}, or better yet, a
 * {@link android.app.Service}.
 */
public class SyncHelper {
    private static final String TAG = makeLogTag("SyncHelper");

    private Context mContext;
    private JekyllDataHandler mJekyllDataHandler;
    private RemoteJekyllDataFetcher mRemoteDataFetcher;

    public SyncHelper(Context context) {
        mContext = context;
        mJekyllDataHandler = new JekyllDataHandler(mContext);
        mRemoteDataFetcher = new RemoteJekyllDataFetcher(mContext);
    }

    public static void requestManualSync(Account mChosenAccount) {
        requestManualSync(mChosenAccount, false);
    }
    public static void requestManualSync(Account mChosenAccount, boolean userDataSyncOnly) {
        if (mChosenAccount != null) {
            LOGD(TAG, "Requesting manual sync for account " + mChosenAccount.name
                    +" userDataSyncOnly="+userDataSyncOnly);
            Bundle b = new Bundle();
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            if (userDataSyncOnly) {
                b.putBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, true);
            }
            ContentResolver.setSyncAutomatically(mChosenAccount, PostsContract.CONTENT_AUTHORITY, true);
            ContentResolver.setIsSyncable(mChosenAccount, PostsContract.CONTENT_AUTHORITY, 1);

            boolean pending = ContentResolver.isSyncPending(mChosenAccount,
                    PostsContract.CONTENT_AUTHORITY);
            if (pending) {
                LOGD(TAG, "Warning: sync is PENDING. Will cancel.");
            }
            boolean active = ContentResolver.isSyncActive(mChosenAccount,
                    PostsContract.CONTENT_AUTHORITY);
            if (active) {
                LOGD(TAG, "Warning: sync is ACTIVE. Will cancel.");
            }

            if (pending || active) {
                LOGD(TAG, "Cancelling previously pending/active sync.");
                ContentResolver.cancelSync(mChosenAccount, PostsContract.CONTENT_AUTHORITY);
            }

            LOGD(TAG, "Requesting sync now.");
            ContentResolver.requestSync(mChosenAccount, PostsContract.CONTENT_AUTHORITY, b);
        } else {
            LOGD(TAG, "Can't request manual sync -- no chosen account.");
        }
    }

    /**
     * Attempts to perform conference data synchronization. The data comes from the remote URL
     * configured in {@link gr.tsagi.jekyllforandroid.app.Config#MANIFEST_URL}. The remote URL
     * must point to a manifest file that, in turn, can reference other files. For more details
     * about conference data synchronization, refer to the documentation at
     * http://code.google.com/p/iosched.
     *
     * @param syncResult (optional) the sync result object to update with statistics.
     * @param account    the account associated with this sync
     * @return Whether or not the synchronization made any changes to the data.
     */
    public boolean performSync(SyncResult syncResult, Account account, Bundle extras) {
        boolean dataChanged = false;

        if (!PrefUtils.isDataBootstrapDone(mContext)) {
            LOGD(TAG, "Sync aborting (data bootstrap not done yet)");
            return false;
        }

        long lastAttemptTime = PrefUtils.getLastSyncAttemptedTime(mContext);
        long now = UIUtils.getCurrentTime(mContext);
        long timeSinceAttempt = now - lastAttemptTime;
        final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        final boolean userDataOnly = extras.getBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, false);

        LOGI(TAG, "Performing sync for account: " + account);
        PrefUtils.markSyncAttemptedNow(mContext);
        long opStart;
        long remoteSyncDuration, choresDuration;

        opStart = System.currentTimeMillis();

        // remote sync consists of these operations, which we try one by one (and tolerate
        // individual failures on each)
        final int OP_REMOTE_SYNC = 0;
        final int OP_USER_SCHEDULE_SYNC = 1;
        final int OP_USER_FEEDBACK_SYNC = 2;

        int[] opsToPerform = userDataOnly ?
                new int[] { OP_USER_SCHEDULE_SYNC } :
                new int[] { OP_REMOTE_SYNC, OP_USER_SCHEDULE_SYNC, OP_USER_FEEDBACK_SYNC};


        for (int op : opsToPerform) {
            try {
                switch (op) {
                    case OP_REMOTE_SYNC:
                        dataChanged |= doRemoteSync();
                        break;
                    case OP_USER_SCHEDULE_SYNC:
                        dataChanged |= doUserScheduleSync(account.name);
                }
            } catch (AuthException ex) {
                syncResult.stats.numAuthExceptions++;

                // if we have a token, try to refresh it
                if (AccountUtils.hasToken(mContext, account.name)) {
                    AccountUtils.refreshAuthToken(mContext);
                } else {
                    LOGW(TAG, "No auth token yet for this account. Skipping remote sync.");
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LOGE(TAG, "Error performing remote sync.");
                increaseIoExceptions(syncResult);
            }
        }
        remoteSyncDuration = System.currentTimeMillis() - opStart;

        // If data has changed, there are a few chores we have to do
        opStart = System.currentTimeMillis();
        if (dataChanged) {
            try {
                performPostSyncChores(mContext);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LOGE(TAG, "Error performing post sync chores.");
            }
        }
        choresDuration = System.currentTimeMillis() - opStart;

        int operations = mJekyllDataHandler.getContentProviderOperationsDone();
        if (syncResult != null && syncResult.stats != null) {
            syncResult.stats.numEntries += operations;
            syncResult.stats.numUpdates += operations;
        }

        if (dataChanged) {
            long totalDuration = choresDuration + remoteSyncDuration;
            LOGD(TAG, "SYNC STATS:\n" +
                    " *  Account synced: " + (account == null ? "null" : account.name) + "\n" +
                    " *  Content provider operations: " + operations + "\n" +
                    " *  Remote sync took: " + remoteSyncDuration + "ms\n" +
                    " *  Post-sync chores took: " + choresDuration + "ms\n" +
                    " *  Total time: " + totalDuration + "ms\n" +
                    " *  Total data read from cache: \n" +
                    (mRemoteDataFetcher.getTotalBytesReadFromCache() / 1024) + "kB\n" +
                    " *  Total data downloaded: \n" +
                    (mRemoteDataFetcher.getTotalBytesDownloaded() / 1024) + "kB");
        }

        LOGI(TAG, "End of sync (" + (dataChanged ? "data changed" : "no data change") + ")");

        updateSyncInterval(mContext, account);

        return dataChanged;
    }

    public static void performPostSyncChores(final Context context) {
        // Update search index
        LOGD(TAG, "Updating search index.");
        context.getContentResolver().update(PostsContract.SearchIndex.CONTENT_URI,
                new ContentValues(), null, null);
    }

    /**
     * Checks if the remote server has new data that we need to import. If so, download
     * the new data and import it into the database.
     *
     * @return Whether or not data was changed.
     * @throws IOException if there is a problem downloading or importing the data.
     */
    private boolean doRemoteSync() throws IOException {
        if (!isOnline()) {
            LOGD(TAG, "Not attempting remote sync because device is OFFLINE");
            return false;
        }

        LOGD(TAG, "Starting remote sync.");

        // Fetch the remote data files via RemoteConferenceDataFetcher
        String[] dataFiles = mRemoteDataFetcher.fetchConferenceDataIfNewer(
                mJekyllDataHandler.getDataTimestamp());

        if (dataFiles != null) {
            LOGI(TAG, "Applying remote data.");
            // save the remote data to the database
            mJekyllDataHandler.applyConferenceData(dataFiles, true);
            LOGI(TAG, "Done applying remote data.");

            // mark that conference data sync succeeded
            PrefUtils.markSyncSucceededNow(mContext);
            return true;
        } else {
            // no data to process (everything is up to date)

            // mark that conference data sync succeeded
            PrefUtils.markSyncSucceededNow(mContext);
            return false;
        }
    }

    /**
     * Checks if there are changes on MySchedule to sync with/from remote AppData folder.
     *
     * @return Whether or not data was changed.
     * @throws IOException if there is a problem uploading the data.
     */
    private boolean doUserScheduleSync(String accountName) throws IOException {
        if (!isOnline()) {
            LOGD(TAG, "Not attempting myschedule sync because device is OFFLINE");
            return false;
        }

        LOGD(TAG, "Starting user data (published) sync.");

        AbstractUserDataSyncHelper helper = UserDataSyncHelperFactory.buildSyncHelper(
                mContext, accountName);
        boolean modified = helper.sync();

        return modified;
    }

    // Returns whether we are connected to the internet.
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void increaseIoExceptions(SyncResult syncResult) {
        if (syncResult != null && syncResult.stats != null) {
            ++syncResult.stats.numIoExceptions;
        }
    }

    private void increaseSuccesses(SyncResult syncResult) {
        if (syncResult != null && syncResult.stats != null) {
            ++syncResult.stats.numEntries;
            ++syncResult.stats.numUpdates;
        }
    }

    public static class AuthException extends RuntimeException {
    }


    public static long calculateRecommendedSyncInterval(final Context context) {
        return Config.AUTO_SYNC_INTERVAL;
    }

    public static void updateSyncInterval(final Context context, final Account account) {
        LOGD(TAG, "Checking sync interval for " + account);
        long recommended = calculateRecommendedSyncInterval(context);
        long current = PrefUtils.getCurSyncInterval(context);
        LOGD(TAG, "Recommended sync interval " + recommended + ", current " + current);
        if (recommended != current) {
            LOGD(TAG, "Setting up sync for account " + account + ", interval " + recommended + "ms");
            ContentResolver.setIsSyncable(account, PostsContract.CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, PostsContract.CONTENT_AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, PostsContract.CONTENT_AUTHORITY,
                    new Bundle(), recommended / 1000L);
            PrefUtils.setCurSyncInterval(context, recommended);
        } else {
            LOGD(TAG, "No need to update sync interval.");
        }
    }
}