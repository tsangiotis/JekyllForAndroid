package gr.tsagi.jekyllforandroid.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.yaml.snakeyaml.Yaml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.github.GithubPush;
import gr.tsagi.jekyllforandroid.github.GithubRaw;
import gr.tsagi.jekyllforandroid.utils.BusProvider;
import gr.tsagi.jekyllforandroid.utils.JekyllRepo;
import gr.tsagi.jekyllforandroid.utils.ShowLoading;
import gr.tsagi.jekyllforandroid.utils.TranslucentBars;

@SuppressLint({ "DefaultLocale", "SimpleDateFormat" })
public class EditPostActivity extends Activity {
    String mUsername;
    String mToken;
    String mDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String mTitle;
    String mCategory;
    String mTags;
    String mContent;

    String message;

    ShowLoading loadAnim;

    private String repo;

    private String selectedImagePath;

    private View mNewPostFormView;
    private View mNewPostStatusView;

    private SharedPreferences settings;


    private static final int GALLERY_INTENT_CALLED = 1;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 2;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        new TranslucentBars(this).tint(true);

        mNewPostFormView = findViewById(R.id.editpost_form);
        mNewPostStatusView = findViewById(R.id.editpost_status);
        Intent intent;

        if(getIntent() != null){
            intent = getIntent();

            if(intent.getStringExtra("post") != null){
        	    message = intent.getStringExtra("post");
        	    clearDraft();
                Log.d("link", message);
                loadAnim = new ShowLoading(mNewPostFormView, mNewPostStatusView);
                loadAnim.showProgress(EditPostActivity.this,true);
        	    new GithubRaw().execute(message, EditPostActivity.this);
            }
            if(intent.getStringExtra("postdate") != null){
        	    mDate = intent.getStringExtra("postdate");
            }
        }

        BusProvider.getInstance().register(this);

        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        /**
         * Restore draft if any is available
         */

        restorePreferences();

        if(mToken == ""){
            Toast.makeText(EditPostActivity.this, "Please login", Toast.LENGTH_LONG ).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_draft:
                clearDraft();
                return true;
            //TODO add image support
//            case R.id.action_add_image:
//                addImage();
//                return true;
            case R.id.action_publish:
            	publishPost();
            	return true;
            case android.R.id.home:
            	if(message!=null)
            		startActivity(new Intent(EditPostActivity.this,PostsListActivity.class));
            	else
            		startActivity(new Intent(EditPostActivity.this,ActionActivity.class));
            	return true;
            case R.id.action_preview:
            	previewMarkdown();
            	return true;
            case R.id.settings:
                Intent intent = new Intent();
                intent.setClass(EditPostActivity.this, SetPreferenceActivity.class);
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addImage (){
        //TODO not implemented yet
        if (Build.VERSION.SDK_INT <19){
            Intent intent = new Intent();
            intent.setType("image/jpeg");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),GALLERY_INTENT_CALLED);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
        }
        }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (null == data) return;
        Uri originalUri = null;
        if (requestCode == GALLERY_INTENT_CALLED) {
            originalUri = data.getData();
        } else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
            originalUri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
        }

        Toast.makeText(EditPostActivity.this,originalUri+"end",Toast.LENGTH_LONG).show();

        EditText content = (EditText)findViewById(R.id.editTextContent);
        content.setText("![]("+originalUri+")");
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private void clearDraft(){
        mTitle    = "";
        mCategory = "";
        mTags     = "";
        mContent  = "";

        setStrings();
    }

    private void restorePreferences(){
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        mUsername = settings.getString("user_login", "");
        mToken = settings.getString("user_status", "");

        mTitle = settings.getString("draft_title", "");
        mCategory = settings.getString("draft_category", "");
        mTags = settings.getString("draft_tags", "");
        mContent = settings.getString("draft_content", "");
        repo =  settings.getString("user_repo", "");

        TextView title = (TextView) findViewById(R.id.editTextTitle);
        TextView category = (TextView) findViewById(R.id.editTextCategory);
        TextView tags = (TextView) findViewById(R.id.editTextTags);
        TextView content = (TextView) findViewById(R.id.editTextContent);

        setStrings();

        if(repo.equals("") && !mUsername.equals("")){
            repo = new JekyllRepo().getName(mUsername);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("user_repo", repo);
            editor.commit();
        }

    }

    private void setStrings(){
        EditText titleT = (EditText)findViewById(R.id.editTextTitle);
        EditText contentT = (EditText)findViewById(R.id.editTextContent);
        EditText categoryT = (EditText)findViewById(R.id.editTextCategory);
        EditText tagsT = (EditText)findViewById(R.id.editTextTags);

        titleT.setText(mTitle);
        categoryT.setText(mCategory);
        tagsT.setText(mTags);
        contentT.setText(mContent);

    }

    public void pushAction() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String, Object> data = new HashMap<String, Object>();



        loadAnim = new ShowLoading(mNewPostFormView, mNewPostStatusView);
        loadAnim.showProgress(EditPostActivity.this,true);

        Yaml yaml = new Yaml();
        String customYaml = prefs.getString("yaml_values", "");
        Log.d("yaml", customYaml);
        Map<String, Object> map = (HashMap<String, Object>) yaml.load(customYaml);
        data.put("tags", mTags.split(","));
        data.put("category", mCategory);
        data.put("title", mTitle);
        data.put("layout", "post");
        if(map != null)
            data.putAll(map);

        String output = "---\n" + yaml.dump(data) + "---\n";

        GithubPush pusher = new GithubPush(EditPostActivity.this);

        try {
            pusher.pushContent(mTitle, mDate, output + mContent);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pushResult(String result){
        loadAnim.showProgress(this, false);
        String message;
        if(result.equals("OK")){
            message = getString(R.string.editpost_publish);
        }
        else
            message = getString(R.string.editpost_fail);
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void publishPost(){

    	final EditText content = (EditText)findViewById(R.id.editTextContent);

    	if(content.getText().toString().isEmpty())
    		Toast.makeText(EditPostActivity.this, R.string.editpost_empty, Toast.LENGTH_LONG).show();
    	else {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		savePreferences();
    		builder.setMessage(R.string.dialog_confirm_update);
    		// Add the buttons
    		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
                       pushAction();
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
    }

    private  void savePreferences(){
        EditText titleT = (EditText)findViewById(R.id.editTextTitle);
        EditText contentT = (EditText)findViewById(R.id.editTextContent);
        EditText categoryT = (EditText)findViewById(R.id.editTextCategory);
        EditText tagsT = (EditText)findViewById(R.id.editTextTags);

        mTitle = titleT.getText().toString();
        mCategory = categoryT.getText().toString();
        mTags = tagsT.getText().toString();
        mContent = contentT.getText().toString();

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("draft_title", mTitle);
        editor.putString("draft_category", mCategory);
        editor.putString("draft_tags", mTags);
        editor.putString("draft_content", mContent);
        editor.commit();
    }

    public void getThePost(HashMap<String,String> map){
        loadAnim.showProgress(EditPostActivity.this, false);
        EditText titleT = (EditText)findViewById(R.id.editTextTitle);
        EditText contentT = (EditText)findViewById(R.id.editTextContent);
        EditText categoryT = (EditText)findViewById(R.id.editTextCategory);
        EditText tagsT = (EditText)findViewById(R.id.editTextTags);

        mTitle = map.get("title");
        mCategory = map.get("category");
        mTags = map.get("tags");
        mContent = map.get("content");

        titleT.setText(mTitle);
        categoryT.setText(mCategory);
        tagsT.setText(mTags);
        contentT.setText(mContent);

    }

    @Override
    protected void onStop() {
        /**
         * Save draft
         */
        savePreferences();
        super.onStop();
    }

    @Override
    protected void onStart() {
        /**
         * Return to draft if any is available
         */
        restorePreferences();

        super.onStart();
    }

    public void previewMarkdown(){
    	savePreferences();
    	if (!mContent.isEmpty()){
    		Intent myIntent = new Intent(EditPostActivity.this, PreviewMarkdownActivity.class);
    		myIntent.putExtra("content", mContent);
            myIntent.putExtra("repo", repo);
    		startActivity(myIntent);
    	}else
    		Toast.makeText(this, "Nothing to preview", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void dumpOutput(HashMap<String, Object> output) {
        if (output.get("error") != null || output.get("result") != null)
            pushResult((String) output.get("result"));
    }
}