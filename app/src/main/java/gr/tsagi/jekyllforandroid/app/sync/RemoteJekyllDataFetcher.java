package gr.tsagi.jekyllforandroid.app.sync;

/**
 * Created by tsagi on 12/12/14.
 */

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import gr.tsagi.jekyllforandroid.app.jfa.model.DataManifest;
import gr.tsagi.jekyllforandroid.app.jfa.model.Post;
import gr.tsagi.jekyllforandroid.app.provider.PostsContract;
import gr.tsagi.jekyllforandroid.app.util.HashUtils;
import gr.tsagi.jekyllforandroid.app.util.Utility;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGD;
import static gr.tsagi.jekyllforandroid.app.util.LogUtils.LOGE;
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


            // Only download if data is newer than oldSha.
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
            String draftsPath = "";

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
                // Set type to 1 for published
                getPostDataFromList(repository, postslist, 1);
            }
            if (!TextUtils.isEmpty(draftsPath)) {
                List<TreeEntry> draftslist = dataService.getTree(repository, draftsPath).getTree();
                // Set type to 0 for draft
                getPostDataFromList(repository, draftslist, 0);
            }


        }

        return null;
    }

    /**
     * Take the List with the posts and parse the posts for data
     */
    private List<Post> getPostDataFromList(Repository repository, List<TreeEntry> postslist,
                                           int published) {

        List<Post> posts = new ArrayList<Post>();
        for (TreeEntry post : postslist) {

            if (post.getType().equals("blob")) {

                String filename = post.getPath();

                // Remove extension from file.
                String[] filenameParts = filename.split("\\.");
                // Use date and filename as id.
                String id = filenameParts[0];

                if (TextUtils.isEmpty(id)) {
                    Log.d(TAG, "No id...");
                    continue;
                }

                Blob postBlob = null;
                try {
                    postBlob = dataService.getBlob(repository, post.getSha()).setEncoding(Blob.ENCODING_UTF8);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                assert postBlob != null;
                String blobBytes = postBlob.getContent();

                posts.add(getDataFromContent(id, blobBytes, published));

            } else {
                try {
                    List<TreeEntry> subdir = dataService.getTree(repository, post.getSha()).getTree();
                    getPostDataFromList(repository, subdir, published);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return posts;
    }

    public Post getDataFromContent(String id, String contentBytes, int type) {

        // Get and insert the new posts information into the database
        String postContent = null;

        // Blobs return with Base64 encoding so we have to UTF-8 them.
        byte[] bytes = Base64.decode(contentBytes, Base64.DEFAULT);
        try {
            postContent = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();

        InputStream is;
        BufferedReader r;

        is = new ByteArrayInputStream(postContent.getBytes());
        // read it with BufferedReader
        r = new BufferedReader(new InputStreamReader(is));
        String line;

        int yaml_dash = 0;
        String yamlStr = "";
        try {
            while ((line = r.readLine()) != null) {
                if (line.equals("---")) {
                    yaml_dash++;
                }
                if (yaml_dash < 2) {
                    if (!line.equals("---"))
                        yamlStr = yamlStr + line + "\n";
                }
                if (yaml_dash >= 2) {
                    if (!line.equals("---")) {
                        if (TextUtils.isEmpty(line))
                            stringBuilder.append("\n\n");
                        else
                            stringBuilder.append(line);
                    }
                }
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Post post = new Post();
        Yaml yaml = new Yaml();

        Map map;
        map = (Map) yaml.load(yamlStr);

        post.id = id;
        post.title = map.get("title").toString();
        if (map.containsKey("tags")) {
            post.tags = map.get("tags").toString().replace("[", "").replace("]", "").split(",");
        }
        if (map.containsKey("category")) {
            post.categories = map.get("category").toString().replace("[", "").replace("]",
                    "").split(",");
        }
        post.content = stringBuilder.toString();
        if (type == 1) {
            post.published = 1;
            int i = post.id.indexOf('-', 1 + id.indexOf('-', 1 + id.indexOf('-')));
            // date is the YYYY-MM-DD on the front of every post.
            post.date = post.id.substring(0, i).replace("-", "");
        } else {
            post.published = 0;
        }

        return post;
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



