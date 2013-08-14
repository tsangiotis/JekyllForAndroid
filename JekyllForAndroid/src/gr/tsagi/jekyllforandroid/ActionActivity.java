package gr.tsagi.jekyllforandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ActionActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        SharedPreferences settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        if(settings.getString("user_status","") == ""){
            login();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Start new post or continue working on your draft
     * @param view
     */
    public void newPost(){
        Intent myIntent = new Intent(getApplicationContext(), NewPostActivity.class);
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
    	Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
    	SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
    	editor.clear();
        editor.commit();    	              
        startActivity(myIntent);
    }
    
}
