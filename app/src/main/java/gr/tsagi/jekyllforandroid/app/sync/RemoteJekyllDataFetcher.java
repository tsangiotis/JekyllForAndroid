package gr.tsagi.jekyllforandroid.app.sync;

/**
 * Created by tsagi on 12/12/14.
 */

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.turbomanage.httpclient.BasicHttpClient;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import gr.tsagi.jekyllforandroid.app.util.TimeUtils;
import gr.tsagi.jekyllforandroid.app.util.Utility;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGD;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGW;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.makeLogTag;

/**
 * Helper class that fetches conference data from the remote server.
 */
public class RemoteJekyllDataFetcher {
    private static final String TAG = makeLogTag(RemoteJekyllDataFetcher.class);

    // Create the needed services
    RepositoryService repositoryService;
    CommitService commitService;
    DataService dataService;
    UserService userService;

    Utility utility;

    String baseCommitSha = "";

    // The directory under which we cache our downloaded files
    private static String CACHE_DIR = "data_cache";

    private Context mContext = null;

    // name of URL override file used for debug purposes
    private static final String URL_OVERRIDE_FILE_NAME = "jfa_manifest_url_override.txt";

    // the set of cache files we have used -- we use this for cache cleanup.
    private HashSet<String> mCacheFilesToKeep = new HashSet<String>();

    // total # of bytes downloaded (approximate)
    private long mBytesDownloaded = 0;

    // total # of bytes read from cache hits (approximate)
    private long mBytesReadFromCache = 0;

    public RemoteJekyllDataFetcher(Context context) {
        mContext = context;

        utility = new Utility(mContext);

        final String token = utility.getToken();

        // Start the client
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);

        // Initiate services
        repositoryService = new RepositoryService(client);
        commitService = new CommitService(client);
        dataService = new DataService(client);
        userService = new UserService(client);
    }

    /**
     * Fetches data from the remote server.
     *
     * @param oldSha The SHA of the Repo to use as a reference; if the remote data
     *               is not newer than this SHA, no data will be downloaded and
     *               this method will return null.
     * @return The data downloaded, or null if there is no data to download
     * @throws IOException if an error occurred during download.
     */
    public String[] fetchConferenceDataIfNewer(String oldSha) throws IOException {

        final String user = utility.getUser();
        final String repo = utility.getRepo();

        LOGD(TAG, user + " - " + repo);
        Repository repository;

        repository = repositoryService.getRepository(user, repo);

        // maybe the user has many branches
        List<RepositoryBranch> branchList = repositoryService.getBranches(repository);
        for (int i = 0; i <= branchList.size(); i++) {
            String name = branchList.get(i).getName();
            if (name.equals("master")) {
                baseCommitSha = repositoryService.getBranches(repository).get(i)
                        .getCommit()
                        .getSha();
                break;
            }

            // No sync when the same sha.


            // Only download if data is newer than oldSha
            // Cloud Storage is very picky with the If-Modified-Since format. If it's in a wrong
            // format, it refuses to serve the file, returning 400 HTTP error. So, if the
            // refTimestamp is in a wrong format, we simply ignore it. But pay attention to this
            // warning in the log, because it might mean unnecessary data is being downloaded.
            if (!TextUtils.isEmpty(oldSha)) {
                if (baseCommitSha.equals(oldSha)) {
                    LOGW(TAG, "No changes have been made to the repo.");
                    return null;
                }
            }

            final String treeSha = commitService.getCommit(repository, baseCommitSha).getSha();

            List<TreeEntry> paths = dataService.getTree(repository, treeSha).getTree();
            // Position of Posts.
            String postsPath = "";
            // Position of drafts.
            String draftsPath= "";

            for (TreeEntry path : paths) {

                LOGD(TAG, path.getPath());
                if (path.getPath().equals("_posts")) {
                    LOGD(TAG, "Found posts!");
                    postsPath = path.getSha();
                }
                if (path.getPath().equals("_drafts")) {
                    LOGD(TAG, "Found drafts!");
                    postsPath = path.getSha();
                }
            }

            if (!TextUtils.isEmpty(postsPath)) {
                List<TreeEntry> postslist = dataService.getTree(repository, postsPath).getTree();
                getPostDataFromList(repository, postslist, 0);
            }
            if (!TextUtils.isEmpty(draftsPath)) {
                List<TreeEntry> draftslist = dataService.getTree(repository, draftsPath).getTree();
                getPostDataFromList(repository, draftslist, 1);
            }


            return null;

            HttpResponse response = httpClient.get(mManifestUrl, null);
            if (response == null) {
                LOGE(TAG, "Request for manifest returned null response.");
                throw new IOException("Request for data manifest returned null response.");
            }

            int status = response.getStatus();
            if (status == HttpURLConnection.HTTP_OK) {
                LOGD(TAG, "Server returned HTTP_OK, so new data is available.");
                mServerTimestamp = getLastModified(response);
                LOGD(TAG, "Server timestamp for new data is: " + mServerTimestamp);
                String body = response.getBodyAsString();
                if (TextUtils.isEmpty(body)) {
                    LOGE(TAG, "Request for manifest returned empty data.");
                    throw new IOException("Error fetching conference data manifest: no data.");
                }
                LOGD(TAG, "Manifest " + mManifestUrl + " read, contents: " + body);
                mBytesDownloaded += body.getBytes().length;
                return processManifest(body);
            } else if (status == HttpURLConnection.HTTP_NOT_MODIFIED) {
                // data on the server is not newer than our data
                LOGD(TAG, "HTTP_NOT_MODIFIED: data has not changed since " + refTimestamp);
                return null;
            } else {
                LOGE(TAG, "Error fetching conference data: HTTP status " + status);
                throw new IOException("Error fetching conference data: HTTP status " + status);
            }
        }

        // Returns the timestamp of the data downloaded from the server

    public String getServerDataTimestamp() {
        return mServerTimestamp;
    }

    /**
     * Returns the remote manifest file's URL. This is stored as a resource in the app,
     * but can be overriden by a file in the filesystem for debug purposes.
     *
     * @return The URL of the remote manifest file.
     */
    private String getManifestUrl() {

        String manifestUrl = Config.MANIFEST_URL;

        // check for an override file
        File urlOverrideFile = new File(mContext.getFilesDir(), URL_OVERRIDE_FILE_NAME);
        if (urlOverrideFile.exists()) {
            try {
                String overrideUrl = FileUtils.readFileAsString(urlOverrideFile).trim();
                LOGW(TAG, "Debug URL override active: " + overrideUrl);
                return overrideUrl;
            } catch (IOException ex) {
                return manifestUrl;
            }
        } else {
            return manifestUrl;
        }
    }

    /**
     * Fetches a file from the cache/network, from an absolute or relative URL. If the
     * file is available in our cache, we read it from there; if not, we will
     * download it from the network and cache it.
     *
     * @param url The URL to fetch the file from. The URL may be absolute or relative; if
     *            relative, it will be considered to be relative to the manifest URL.
     * @return The contents of the file.
     * @throws IOException If an error occurs.
     */
    private String fetchFile(String url) throws IOException {
        // If this is a relative url, consider it relative to the manifest URL
        if (!url.contains("://")) {
            if (TextUtils.isEmpty(mManifestUrl) || !mManifestUrl.contains("/")) {
                LOGE(TAG, "Could not build relative URL based on manifest URL.");
                return null;
            }
            int i = mManifestUrl.lastIndexOf('/');
            url = mManifestUrl.substring(0, i) + "/" + url;
        }

        LOGD(TAG, "Attempting to fetch: " + sanitizeUrl(url));

        // Check if we have it in our cache first
        String body = null;
        try {
            body = loadFromCache(url);
            if (!TextUtils.isEmpty(body)) {
                // cache hit
                mBytesReadFromCache += body.getBytes().length;
                mCacheFilesToKeep.add(getCacheKey(url));
                return body;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            LOGE(TAG, "IOException getting file from cache.");
            // proceed anyway to attempt to download it from the network
        }

        // We don't have the file on cache, so download it
        LOGD(TAG, "Cache miss. Downloading from network: " + sanitizeUrl(url));
        BasicHttpClient client = new BasicHttpClient();
        client.setRequestLogger(mQuietLogger);
        HttpResponse response = client.get(url, null);

        if (response == null) {
            throw new IOException("Request for URL " + sanitizeUrl(url) + " returned null response.");
        }

        LOGD(TAG, "HTTP response " + response.getStatus());
        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            body = response.getBodyAsString();
            if (TextUtils.isEmpty(body)) {
                throw new IOException("Got empty response when attempting to fetch " +
                        sanitizeUrl(url));
            }
            LOGD(TAG, "Successfully downloaded from network: " + sanitizeUrl(url));
            mBytesDownloaded += body.getBytes().length;
            writeToCache(url, body);
            mCacheFilesToKeep.add(getCacheKey(url));
            return body;
        } else {
            LOGE(TAG, "Failed to fetch from network: " + sanitizeUrl(url));
            throw new IOException("Request for URL " + sanitizeUrl(url) +
                    " failed with HTTP error " + response.getStatus());
        }
    }

    /**
     * Returns the cache file where we store our cache of the response of the given URL.
     *
     * @param url The URL for which to return the cache file.
     * @return The cache file.
     */
    private File getCacheFile(String url) {
        String cacheKey = getCacheKey(url);
        return new File(mContext.getCacheDir() + File.separator + CACHE_DIR + File.separator +
                cacheKey);
    }

    // Creates the cache directory, if it doesn't exist yet
    private void createCacheDir() throws IOException {
        File dir = new File(mContext.getCacheDir() + File.separator + CACHE_DIR);
        if (!dir.exists() && !dir.mkdir()) {
            throw new IOException("Failed to mkdir: " + dir);
        }
    }


    /**
     * Loads our cached content corresponding to the given URL.
     *
     * @param url The URL for which to load the cached response.
     * @return The cached response corresponding to the URL; or null if the given URL
     * does not exist in our cache.
     * @throws IOException If there is an error reading the cache.
     */
    private String loadFromCache(String url) throws IOException {
        String cacheKey = getCacheKey(url);
        File cacheFile = getCacheFile(url);
        if (cacheFile.exists()) {
            LOGD(TAG, "Cache hit " + cacheKey + " for " + sanitizeUrl(url));
            return FileUtils.readFileAsString(cacheFile);
        } else {
            LOGD(TAG, "Cache miss " + cacheKey + " for " + sanitizeUrl(url));
            return null;
        }
    }

    /**
     * Writes a file to the cache.
     *
     * @param url  The URL from which the contents were retrieved.
     * @param body The contents retrieved from the given URL.
     * @throws IOException If there is a problem writing the file.
     */
    private void writeToCache(String url, String body) throws IOException {
        String cacheKey = getCacheKey(url);
        File cacheFile = getCacheFile(url);
        createCacheDir();
        FileUtils.writeFile(body, cacheFile);
        LOGD(TAG, "Wrote to cache " + cacheKey + " --> " + sanitizeUrl(url));
    }

    /**
     * Returns the cache key to be used to store the given URL. The cache key is the
     * file name under which the contents of the URL are stored.
     *
     * @param url The URL.
     * @return The cache key (guaranteed to be a valid filename)
     */
    private String getCacheKey(String url) {
        return HashUtils.computeWeakHash(url.trim()) + String.format("%04x", url.length());
    }

    // Sanitize a URL for logging purposes (only the last component is left visible).
    private String sanitizeUrl(String url) {
        int i = url.lastIndexOf('/');
        if (i >= 0 && i < url.length()) {
            return url.substring(0, i).replaceAll("[A-za-z]", "*") +
                    url.substring(i);
        } else return url.replaceAll("[A-za-z]", "*");
    }

    private static final String MANIFEST_FORMAT = "iosched-json-v1";

    /**
     * Process the data manifest and download data files referenced from it.
     *
     * @param manifestJson The JSON of the manifest file.
     * @return The contents of the set of files referenced from the manifest, or null
     * if none could be retrieved.
     * @throws IOException If an error occurs while retrieving information.
     */
    private String[] processManifest(String manifestJson) throws IOException {
        LOGD(TAG, "Processing data manifest, length " + manifestJson.length());

        DataManifest manifest = new Gson().fromJson(manifestJson, DataManifest.class);
        if (manifest.format == null || !manifest.format.equals(MANIFEST_FORMAT)) {
            LOGE(TAG, "Manifest has invalid format spec: " + manifest.format);
            throw new IOException("Invalid format spec on manifest:" + manifest.format);
        }

        if (manifest.data_files == null || manifest.data_files.length == 0) {
            LOGW(TAG, "Manifest does not list any files. Nothing done.");
            return null;
        }

        LOGD(TAG, "Manifest lists " + manifest.data_files.length + " data files.");
        String[] jsons = new String[manifest.data_files.length];
        for (int i = 0; i < manifest.data_files.length; i++) {
            String url = manifest.data_files[i];
            LOGD(TAG, "Processing data file: " + sanitizeUrl(url));
            jsons[i] = fetchFile(url);
            if (TextUtils.isEmpty(jsons[i])) {
                LOGE(TAG, "Failed to fetch data file: " + sanitizeUrl(url));
                throw new IOException("Failed to fetch data file " + sanitizeUrl(url));
            }
        }

        LOGD(TAG, "Got " + jsons.length + " data files.");
        cleanUpCache();
        return jsons;
    }

    // Delete unnecessary files from our cache
    private void cleanUpCache() {
        LOGD(TAG, "Starting cache cleanup, " + mCacheFilesToKeep.size() + " URLs to keep.");
        File dir = new File(mContext.getCacheDir() + File.separator + CACHE_DIR);
        if (!dir.exists()) {
            LOGD(TAG, "Cleanup complete (there is no cache).");
            return;
        }

        int deleted = 0, kept = 0;
        for (File file : dir.listFiles()) {
            if (mCacheFilesToKeep.contains(file.getName())) {
                LOGD(TAG, "Cache cleanup: KEEEPING " + file.getName());
                ++kept;
            } else {
                LOGD(TAG, "Cache cleanup: DELETING " + file.getName());
                file.delete();
                ++deleted;
            }
        }

        LOGD(TAG, "End of cache cleanup. " + kept + " files kept, " + deleted + " deleted.");
    }

    public long getTotalBytesDownloaded() {
        return mBytesDownloaded;
    }

    public long getTotalBytesReadFromCache() {
        return mBytesReadFromCache;
    }

    private String getLastModified(HttpResponse resp) {
        if (!resp.getHeaders().containsKey("Last-Modified")) {
            return "";
        }

        List<String> s = resp.getHeaders().get("Last-Modified");
        return s.isEmpty() ? "" : s.get(0);
    }

    /**
     * A type of ConsoleRequestLogger that does not log requests and responses.
     */
    private RequestLogger mQuietLogger = new ConsoleRequestLogger() {
        @Override
        public void logRequest(HttpURLConnection uc, Object content) throws IOException {
        }

        @Override
        public void logResponse(HttpResponse res) {
        }
    };



