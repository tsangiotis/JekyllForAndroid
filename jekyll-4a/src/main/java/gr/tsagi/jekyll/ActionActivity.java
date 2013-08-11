package gr.tsagi.jekyll;

import android.app.Activity;
import android.content.Context;
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
                "gr.tsagi.jekyll", Context.MODE_PRIVATE);
        if(settings.getString("user_status","") == ""){
            login();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                login();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void newPost(View view){
        Intent myIntent = new Intent(view.getContext(), NewPostActivity.class);
        startActivity(myIntent);
    }

    public void login(){
        Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(myIntent);
    }
    
}
