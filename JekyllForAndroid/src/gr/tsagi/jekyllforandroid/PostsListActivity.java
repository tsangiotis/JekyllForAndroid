package gr.tsagi.jekyllforandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tsagi on 9/9/13.
 */
public class PostsListActivity extends Activity {

//    public static UberdustParserFragment newInstance() {
//        return new UberdustParserFragment();
//    }

    String date;
    String title;
    String content;
    String postid;
    
    String mUsername;
    String mToken;
    
    String json_html = "";
    String old_json;

    private SharedPreferences settings;
    
    private View mListView;
    private View mListStatusView;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> postList;
    // creating new HashMap
    HashMap<String, String> map;

    // url to make request
    String j_url;
    
    List<String> urls = new ArrayList<String>();
    List<String> dates = new ArrayList<String>();

    // JSON Node names
    private static final String TAG_TITLE = "title";
    private static final String TAG_ID = "id";
    private static final String TAG_DATE = "published_on";
    private static final String TAG_POSTS = "posts";

    // contacts JSONArray
    JSONArray posts = null;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);
        getActionBar().setSubtitle("Alpha feature");
        restorePreferences();
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        mListView = findViewById(R.id.posts_list);
        mListStatusView = findViewById(R.id.postslist_status);
        
        j_url = "http://" + mUsername +".github.com" + "/json/";
        
        if(old_json.equals(""))
        	new HtmlToJson().execute(j_url);
        else
        	new ParsePostsData().execute(old_json);
        	new HtmlToJson().execute(j_url);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        startActivity(new Intent(PostsListActivity.this,ActionActivity.class)); 
        return true;
    }
    
    private void restorePreferences(){
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        mUsername = settings.getString("user_login", "");
        mToken = settings.getString("user_status", "");
        old_json = settings.getString("json_html", "");
        
    }
    
    private class HtmlToJson extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {

            String url = params[0];
            
            try{
            	HttpClient client = new DefaultHttpClient();
            	HttpGet request = new HttpGet(url);
            	HttpResponse response = client.execute(request);

            	InputStream in = response.getEntity().getContent();
            	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            	StringBuilder str = new StringBuilder();
            	String line = null;
            	while((line = reader.readLine()) != null)
            	{
            		str.append(line);
            	}
            	in.close();
            	json_html = str.toString();
            
            } catch (ClientProtocolException e) {
            	//no network
            	e.printStackTrace();
            } catch (IOException e) {
            	//http problem
            	e.printStackTrace();
            } catch (IllegalStateException e) {
            	e.printStackTrace();
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void aVoid) {
            if(!json_html.equals(old_json))
            	new ParsePostsData().execute(json_html);
            Log.d("works", json_html);
        }
    }

    private class ParsePostsData extends AsyncTask<String, Void, String> {
    	
    	@Override
        protected void onPreExecute() {
            showProgress(true);
        }
    	
    	@Override
        protected String doInBackground(String... params) {            
            
        	String json_str = params[0];
            JSONObject json = null;
            
            // Hashmap for ListView
            postList = new ArrayList<HashMap<String, String>>();
            // getting JSON string from URL
            //JSONObject json = new JSONObject();


            try {
            	json = new JSONObject(json_str);
                // Getting Array of Readings
                posts = json.getJSONArray(TAG_POSTS);

                // looping through All Contacts
                for(int i = 0; i < posts.length(); i++){
                    JSONObject r = posts.getJSONObject(i);

                    // Storing each json item in variable
                    title = r.getString(TAG_TITLE);
                    postid = r.getString(TAG_ID);
                    date = r.getString(TAG_DATE);
                    
                    String[] separatedId = postid.split("\\/");
                    for(int j=0; j < separatedId.length; j++){
                    	if(separatedId[j].startsWith("20")){
                    		String url = "https://raw.github.com/"+ mUsername + "/"+ mUsername+".github.com/master/_posts/"
                    	+separatedId[j] +"-"+ separatedId[j+1] +"-"+ separatedId[j+2] + "-"+separatedId[j+3]+".md";
                    		dates.add(separatedId[j] +"-"+ separatedId[j+1] +"-"+ separatedId[j+2]);
                    		urls.add(url);
                    		break;
                    	}
                    }
                    
                    // creating new HashMap
                    map = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    map.put(TAG_DATE, date);
                    map.put(TAG_TITLE, title);

                    // adding HashList to ArrayList
                    postList.add(map);

                }

            }catch (JSONException e) {
                    e.printStackTrace();
                    return "noJSON";
            }
            return "OK";
        }
    	
    	@Override
        protected void onCancelled() {
            showProgress(false);
        }
        
        @Override
        protected void onPostExecute(String result) {
            /**
             * Updating parsed JSON data into ListView
             * */
        	showProgress(false);
        	
        	if(result.equals("noJSON")){
        		jsonTool();
        	}
        	else{
        	ListView postsList=(ListView)findViewById(R.id.posts_list);
            adapter = new SimpleAdapter(PostsListActivity.this, postList,
                    R.layout.list_view,
                    new String[] { TAG_DATE, TAG_TITLE }, new int[] {
                    R.id.pdate, R.id.ptitle });

            postsList.setAdapter(adapter);
            postsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    			public void onItemClick(AdapterView<?> parent, View view,
    					int position, long id) {
    		    	Intent editIntent = new Intent(PostsListActivity.this, EditPostActivity.class);
    		    	editIntent.putExtra("post", urls.get(position));
    		    	editIntent.putExtra("postdate", dates.get(position));
    		    	startActivity(editIntent);
    			}
    		});
        	}
        }
    }
    
    private Runnable jsonTool = new Runnable() {
        @Override
        public void run() {
            uploadJsonTool();
        }
    };


    private void jsonTool() {
        runOnUiThread(jsonTool);
    }
    
    private void uploadJsonTool(){
    	
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		
    		builder.setMessage(R.string.dialog_push_json);
        
    		// Add the buttons
    		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   /**
    	        	    * Publish post
    	        	    */
    	        	   new PushFile().execute();
    	           }
    	       });
    		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	               // User cancelled the dialog
    	           }
    	       });

    		// Create the AlertDialog
    		AlertDialog dialog = builder.create();
    		dialog.show();
    	}
    
    class PushFile extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                // based on http://swanson.github.com/blog/2011/07/23/digging-around-the-github-api-take-2.html
                // initialize github client
                GitHubClient client = new GitHubClient();

                client.setOAuth2Token(mToken);

                // create needed services
                RepositoryService repositoryService = new RepositoryService();
                CommitService commitService = new CommitService(client);
                DataService dataService = new DataService(client);

                // get some sha's from current state in git
                Repository repository =  repositoryService.getRepository(mUsername, mUsername+".github.com");
                String baseCommitSha = repositoryService.getBranches(repository).get(0).getCommit().getSha();
                RepositoryCommit baseCommit = commitService.getCommit(repository, baseCommitSha);
                String treeSha = baseCommit.getSha();
                
                String completeContent = "---\n" +
                        "title: json" + "\n" +
                        "---\n" +
                        "{\"homepage\":\"{{ site.production_url }}\",\"name\":\"{{ site.title }}\",\"description\":\"{{ site.tagline }}\""+
                        ",\"author\":\"{{ site.author.name }}\",\"posts\":[{% for post in site.posts %}"+
                        "{\"url\":\"{{ site.production_url }}{{ post.url }}\",\"title\""+
                        ":\"{{ post.title }}\",\"id\": \"{{ post.id }}\",\"published_on\":\"{{ post.date | date: \"%-d %B %Y\" }}\"}"+
                        "{% if forloop.rindex0 > 0 %},{% endif %}{% endfor %}]}";

                // create new blob with data
                Blob blob = new Blob();
                blob.setContent(completeContent).setEncoding(Blob.ENCODING_UTF8);
                String blob_sha = dataService.createBlob(repository, blob);
                Tree baseTree = dataService.getTree(repository, treeSha);

                // set path
                String path = "json/index.html";

                // create new tree entry
                TreeEntry treeEntry = new TreeEntry();
                
                // working
                treeEntry.setPath(path);
                treeEntry.setMode(TreeEntry.MODE_BLOB);
                treeEntry.setType(TreeEntry.TYPE_BLOB);
                treeEntry.setSha(blob_sha);
                treeEntry.setSize(blob.getContent().length());
                Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
                entries.add(treeEntry);
                Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

                // create commit
                Commit commit = new Commit();
                commit.setMessage("Json generator by Jekyll for Android");
                commit.setTree(newTree);
                List<Commit> listOfCommits = new ArrayList<Commit>();
                listOfCommits.add(new Commit().setSha(baseCommitSha));
                // listOfCommits.containsAll(base_commit.getParents());
                commit.setParents(listOfCommits);
                // commit.setSha(base_commit.getSha());
                Commit newCommit = dataService.createCommit(repository, commit);

                // create resource
                TypedResource commitResource = new TypedResource();
                commitResource.setSha(newCommit.getSha());
                commitResource.setType(TypedResource.TYPE_COMMIT);
                commitResource.setUrl(newCommit.getUrl());

                // get master reference and update it
                Reference reference = dataService.getReference(repository, "heads/master");
                reference.setObject(commitResource);
                dataService.editReference(repository, reference, true);

                // success

            } catch (Exception e) {
                // error
                e.printStackTrace();
            }
            return null;
        }
    }
    
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListStatusView.setVisibility(View.VISIBLE);
            mListStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                        	mListStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mListView.setVisibility(View.VISIBLE);
            mListView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                        	mListView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mListStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
    @Override
    protected void onStop() {
        savePreferences();
        super.onStop();
    }
    
    private  void savePreferences(){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("json_html", json_html);
        editor.commit();
    }
}