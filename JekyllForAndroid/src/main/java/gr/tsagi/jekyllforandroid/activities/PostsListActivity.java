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

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.github.GithubPush;
import gr.tsagi.jekyllforandroid.utils.BusProvider;
import gr.tsagi.jekyllforandroid.utils.ParsePostData;
import gr.tsagi.jekyllforandroid.utils.ShowLoading;
import gr.tsagi.jekyllforandroid.utils.TranslucentBars;

/**
 * Created by tsagi on 9/9/13.
 */

public class PostsListActivity extends Activity {

    String mUsername;
    String mToken;

    ListAdapter adapter;

    ShowLoading loadAnim;

    private String repo;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);

        new TranslucentBars(this).tint(true);

        restorePreferences();

        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        BusProvider.getInstance().register(this);

        if(repo.isEmpty()){
            Toast.makeText(PostsListActivity.this,
                    "There is something wrong with your jekyll repo", Toast.LENGTH_LONG).show();
        }
        else {
            View mListView = findViewById(R.id.posts_list);
            View mListStatusView = findViewById(R.id.postslist_status);

            loadAnim = new ShowLoading(mListView, mListStatusView);
            loadAnim.showProgress(PostsListActivity.this,true);
            new ParsePostData().execute("http://"+ repo + "/json", PostsListActivity.this);
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
        repo = settings.getString("user_repo", "");

    }

    public void uploadJsonTool(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.dialog_push_json);
        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                /**
                 * Publish post
                 */
                uploadJson();
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

    private void uploadJson() {
            loadAnim.showProgress(this, true);
            GithubPush gitAgent = new GithubPush(this);
            try {
                gitAgent.pushJson();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }

    public void pushResult(String result){
        loadAnim.showProgress(PostsListActivity.this, false);
        String message;
        if(result.equals("OK")){
            message = getString(R.string.success);
        }
        else
            message = getString(R.string.fail);
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
        finish();
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
                if (!urls.isEmpty()){
                    editIntent.putExtra("post", urls.get(position));
                    editIntent.putExtra("postdate", dates.get(position));
                    startActivity(editIntent);
                }
            }
        });
    }

    @Subscribe
    public void dumpOutput(HashMap<String, Object> output) {
        if (output.get("error") != null)
            uploadJsonTool();
        if(output.get("result") != null)
            pushResult((String) output.get("result"));
        if(output.get("postsList") != null)
            updateList((ArrayList<HashMap<String,String>>)output.get("postsList"),
                    (List<String>)output.get("dates"),
                    (List<String>)output.get("urls"));

    }
}