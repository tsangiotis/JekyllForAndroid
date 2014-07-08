package gr.tsagi.jekyllforandroid.activities;

import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.yaml.snakeyaml.tokens.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.cketti.library.changelog.ChangeLog;
import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.fragments.EntryListFragment;
import gr.tsagi.jekyllforandroid.github.GithubPush;
import gr.tsagi.jekyllforandroid.utils.BusProvider;
import gr.tsagi.jekyllforandroid.utils.ParsePostData;
import gr.tsagi.jekyllforandroid.utils.ShowLoading;
import gr.tsagi.jekyllforandroid.utils.TranslucentBars;

/**
 * Created by tsagi on 9/9/13.
 */

public class PostsListActivity extends FragmentActivity {

    String mUsername;
    String mToken;

    ListAdapter adapter;

    ShowLoading loadAnim;

    private String[] mNavTitles;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private int prevPosition;

    private ListView mDrawerList;

    private String repo;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);

//        new TranslucentBars(this).tint(true);
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("user_login", "");
        editor.apply();

        if(settings.getString("user_status","").equals("")){
            login();
        }

        DrawerSetup();

        restorePreferences();

        BusProvider.getInstance().register(PostsListActivity.this);

        if(repo.isEmpty()){
            Toast.makeText(PostsListActivity.this,
                    "There is something wrong with your jekyll repo", Toast.LENGTH_LONG).show();
        }
        else {
            View mListView = findViewById(R.id.posts_list);
            View mListStatusView = findViewById(R.id.postslist_status);

        }
        //Set default screen

        selectItem(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        restorePreferences();
        Log.d("Resuming", "Restarted");
        DrawerSetup();
        Fragment frg = null;
        frg = new EntryListFragment();
        frg = getSupportFragmentManager().findFragmentByTag("ListFragment");
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first

        // Activity being restarted from stopped state
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action, menu);
        // Just for the logout
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutDialog();
                return true;
            case R.id.action_new:
                newPost();
                return true;
            case R.id.settings:
                Intent intent = new Intent();
                intent.setClass(PostsListActivity.this, SetPreferenceActivity.class);
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_new).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void DrawerSetup(){
        mNavTitles = getResources().getStringArray(R.array.nav_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerTitle = getResources().getString(R.string.app_name);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mNavTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // just styling option add shadow the right edge of the drawer
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
////        Show Changelog
//        ChangeLog cl = new ChangeLog(this);
//        if (cl.isFirstRun()) {
//            cl.getLogDialog().show();
//        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        // Create a new fragment and specify the planet to show based on
        // position
        Fragment fragment = null;
        try {
            fragment = new EntryListFragment();
            Bundle args = new Bundle();
            args.putInt(EntryListFragment.ARG_PDSTATUS, position);
            args.putString(EntryListFragment.ARG_REPO, repo);
            if (args != null) {
                fragment.setArguments(args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, "ListFragment")
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mNavTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * Start new post or continue working on your draft
     */
    public void newPost(){
        Intent myIntent = new Intent(PostsListActivity.this, EditPostActivity.class);
        startActivity(myIntent);
    }

    /**
     * Logout and clear settings
     */
    public void logoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Shared preferences and Intent settings
        // before logout ask user and remind him any draft posts

        final SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);

        if (sharedPreferences.getString("draft_content", "").equals(""))
            builder.setMessage(R.string.dialog_logout_nodraft);
        else
            builder.setMessage(R.string.dialog_logout_draft);

        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                // Clear credentials and Drafts
                login();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show it
        dialog.show();

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
        //loadAnim.showProgress(PostsListActivity.this, false);
        String message;
        if(result.equals("OK")){
            message = getString(R.string.success);
        }
        else
            message = getString(R.string.fail);
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
        finish();
    }


    @Subscribe
    public void dumpOutput(HashMap<String, Object> output) {
        if (output.get("error") != null)
            uploadJsonTool();
        if (output.get("result") != null)
            pushResult((String) output.get("result"));
        if (output.get("postsList") != null) {
            EntryListFragment listFragment = (EntryListFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (listFragment != null)
                listFragment.updateList((ArrayList<HashMap<String, String>>) output.get("postsList"),
                    (List<String>) output.get("dates"),
                    (List<String>) output.get("urls"));
            else
                Log.d("Fragment","It's null");
        }
    }

    private void login(){
        Intent myIntent = new Intent(PostsListActivity.this, LoginActivity.class);
        SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.commit();

        startActivity(myIntent);
    }
}

