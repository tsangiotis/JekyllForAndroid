package gr.tsagi.jekyllforandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
    String yamlcontent;

    private String extraYAML;
    private String extra;
    private String subdir;

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
        
        mNewPostFormView = findViewById(R.id.newpost_form);
        mNewPostStatusView = findViewById(R.id.newpost_status);
        
        Intent intent = getIntent();
        if(intent.getStringExtra("post") != null){
        	message = intent.getStringExtra("post");
        	clearDraft();
            setStrings();
            Log.d("link", message);
        	new getPost().execute(message);
        }
        if(intent.getStringExtra("postdate") != null){
        	mDate = intent.getStringExtra("postdate");
        }
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /**
         * Restore draft if any is available
         */
        
        restorePreferences();
        setStrings();

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
            case R.id.action_add_image:
                addImage();
                return true;
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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
    
    private class getPost extends AsyncTask<String, Void, Void> {
    	
    	@Override
        protected void onPreExecute() {
            showProgress(true, getString(R.string.getpost_progress));
        }

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
            	int yaml_dash=0;
            	String yaml =null;
            	HashMap<String, String[]> map;
            	while((line = reader.readLine()) != null)
            	{
            		if(line.equals("---")){
            			yaml_dash++;
            		}
            		if (yaml_dash!=2)
            			if(!line.equals("---"))
                            yaml = yaml + line + "\n";

            		if (yaml_dash==2){
            			if(!line.equals("---"))
            				if(line.isEmpty())
            					str.append("\n");
            				else
            					str.append(line);
            		}
            	}
                in.close();
                yamlFromString(yaml);
                mContent = str.toString().replaceAll("\n","\n\n");
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
        	showProgress(false, "...");
            setStrings();
        }
    }

    public void yamlFromString(String yamlStr) {
        Yaml yaml = new Yaml();
        HashMap<String, String[]> map = (HashMap<String, String[]>) yaml.load(yamlStr);

        mTitle = String.valueOf(map.get("title"));
        mCategory = String.valueOf(map.get("category"));
        mTags = String.valueOf(map.get("tags")).replace("[", "").replace("]", "");
    }

    public void addImage (){
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

        Toast.makeText(getApplicationContext(),originalUri+"end",Toast.LENGTH_LONG).show();

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
    }
    
    private void publishPost(){
    	
    	final EditText content = (EditText)findViewById(R.id.editTextContent);
    	
    	if(content.getText().toString().isEmpty())
    		Toast.makeText(getApplicationContext(), R.string.newpost_empty, Toast.LENGTH_LONG).show();
    	else {
    	
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
    		savePreferences();
    		builder.setMessage(R.string.dialog_confirm_update);
        
    		// Add the buttons
    		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   /**
    	        	    * Publish post
    	        	    */
    	        	   new UpdateFile().execute();
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
        setStrings();
        
        super.onStart();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show, String message) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            TextView loadingText = (TextView) findViewById(R.id.newpost_status_message);

            if(!message.isEmpty())
                loadingText.setText(message);
            else
                loadingText.setText("None");

            mNewPostStatusView.setVisibility(View.VISIBLE);
            mNewPostStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mNewPostStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mNewPostFormView.setVisibility(View.VISIBLE);
            mNewPostFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mNewPostFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mNewPostStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mNewPostFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class UpdateFile extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            hideSoftKeyboard(EditPostActivity.this);
            showProgress(true, getString(R.string.newpost_progress));
        }
        

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                // based on http://swanson.github.com/blog/2011/07/23/digging-around-the-github-api-take-2.html
                // initialize github client
                GitHubClient client = new GitHubClient();
                EditText titleT = (EditText)findViewById(R.id.editTextTitle);
                EditText contentT = (EditText)findViewById(R.id.editTextContent);
                EditText categoryT = (EditText)findViewById(R.id.editTextCategory);
                EditText tagsT = (EditText)findViewById(R.id.editTextTags);

                loadPref();

                mTitle = titleT.getText().toString();
                mCategory = categoryT.getText().toString();
                mTags = tagsT.getText().toString();
                mContent = contentT.getText().toString();

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
                        "layout: post\n" +
                        "title: " + '"' + mTitle + '"' + "\n" +
                        "description: "+ '"' + '"'+" \n" +
                        "category: " + mCategory + "\n" +
                        "tags: [" + mTags + "]"+ "\n" +
                        extraYAML + "\n" +
                        "---\n" +
                        extra + "\n" +
                        mContent;

                // create new blob with data
                Blob blob = new Blob();
                blob.setContent(completeContent).setEncoding(Blob.ENCODING_UTF8);
                String blob_sha = dataService.createBlob(repository, blob);
                Tree baseTree = dataService.getTree(repository, treeSha);

                // set path
                String path = mDate + "-" + mTitle.toLowerCase().replace(' ', '-')
                        .replace(",","").replace("!","").replace(".","") + ".md";

                // create new tree entry
                TreeEntry treeEntry = new TreeEntry();
                
                // working
                if(subdir!=null)
                    treeEntry.setPath("_posts/" + subdir + "/" + path);
                else
                    treeEntry.setPath("_posts/" + path);
                treeEntry.setMode(TreeEntry.MODE_BLOB);
                treeEntry.setType(TreeEntry.TYPE_BLOB);
                treeEntry.setSha(blob_sha);
                treeEntry.setSize(blob.getContent().length());
                Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
                entries.add(treeEntry);
                Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

                // create commit
                Commit commit = new Commit();
                commit.setMessage("Update/new Post from Jekyll for Android app");
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

        @Override
        protected void onCancelled() {
            showProgress(false, "...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            showProgress(false, "...");
            Toast.makeText(EditPostActivity.this, "Post published!", Toast.LENGTH_LONG ).show();

            /**
             * Clear fields for the next post
             */
            clearDraft();
            setStrings();

            finish();
        }
    }

    private void loadPref(){
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        extraYAML = mySharedPreferences.getString("yaml_values", "") + "\n";
        extra = mySharedPreferences.getString("other_values", "") + "\n";
        subdir = mySharedPreferences.getString("posts_subdir", "");
    }
    
    public void previewMarkdown(){
    	savePreferences();
    	if (!mContent.isEmpty()){
    		Intent myIntent = new Intent(getApplicationContext(), PreviewMarkdownActivity.class);
    		myIntent.putExtra("content", mContent);
    		startActivity(myIntent);
    	}else
    		Toast.makeText(this, "Nothing to preview", Toast.LENGTH_SHORT).show();
    }


}