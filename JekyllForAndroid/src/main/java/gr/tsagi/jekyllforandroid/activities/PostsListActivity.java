package gr.tsagi.jekyllforandroid.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.utils.GithubPush;
import gr.tsagi.jekyllforandroid.utils.ParsePostData;
import gr.tsagi.jekyllforandroid.utils.ShowLoading;

/**
 * Created by tsagi on 9/9/13.
 */

public class PostsListActivity extends Activity {

    String date;
    String title;
    String content;
    String postid;
    
    String mUsername;
    String mToken;
    
    String json_html = "";
    String old_json;

    private SharedPreferences settings;
    private String subdir;
    
    // Hashmap for ListView
    ArrayList<HashMap<String, String>> postList;
    // creating new HashMap
    HashMap<String, String> map;

    // url to make request
    String j_url;

    // JSON Node names
    private static final String TAG_TITLE = "title";
    private static final String TAG_ID = "id";
    private static final String TAG_DATE = "published_on";
    private static final String TAG_POSTS = "posts";

    // contacts JSONArray
    JSONArray posts = null;
    ListAdapter adapter;

    ShowLoading loadAnim;

    private String repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);
        restorePreferences();
        
        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        
        if(repo.isEmpty()){
            j_url="";
            Toast.makeText(PostsListActivity.this,
                    "There is something wrong with your jekyll repo", Toast.LENGTH_LONG).show();
        }
        else {
            View mListView = findViewById(R.id.posts_list);
            View mListStatusView = findViewById(R.id.postslist_status);

            loadAnim = new ShowLoading(mListView, mListStatusView);
            loadAnim.showProgress(PostsListActivity.this,true);
            new ParsePostData().execute("http://"+ repo + "/json", this);
        }


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
        repo = settings.getString("user_repo", "");
        
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
    	        	   GithubPush gitAgent = new GithubPush(PostsListActivity.this);
                       gitAgent.pushJson();
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

    public void updateList(ArrayList<HashMap<String, String>> postList, final List<String> dates, final List<String> urls) {
        loadAnim.showProgress(PostsListActivity.this,false);
        ListView postsList=(ListView)findViewById(R.id.posts_list);
        adapter = new SimpleAdapter(PostsListActivity.this, postList,
                R.layout.list_view,
                new String[] { "published_on", "title" }, new int[] {
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