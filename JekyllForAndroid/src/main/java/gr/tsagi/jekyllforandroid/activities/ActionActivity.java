package gr.tsagi.jekyllforandroid.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.cketti.library.changelog.ChangeLog;
import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.utils.TranslucentBars;

public class ActionActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        new TranslucentBars(this).tint(true);

        SharedPreferences settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        if(settings.getString("user_status","").equals("")){
            login();
        }

//        Show Changelog
        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }
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
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutDialog();
                return true;
            case R.id.action_new:
                newPost();
                return true;
            case R.id.action_list:
                listPosts();
                return true;
            case R.id.settings:
                Intent intent = new Intent();
                intent.setClass(ActionActivity.this, SetPreferenceActivity.class);
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Start new post or continue working on your draft
     */
    public void newPost(){
        Intent myIntent = new Intent(ActionActivity.this, EditPostActivity.class);
        startActivity(myIntent);
    }
    
    public void listPosts(){
        Intent myIntent = new Intent(ActionActivity.this, PostsListActivity.class);
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
    
    private void login(){
    	Intent myIntent = new Intent(ActionActivity.this, LoginActivity.class);
    	SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
    	editor.clear();
        editor.commit();

        startActivity(myIntent);
    }
    
}
