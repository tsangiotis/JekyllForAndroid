package gr.tsagi.jekyllforandroid;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tsagi on 9/9/13.
 */
public class PostsListActivity extends ListActivity {

//    public static UberdustParserFragment newInstance() {
//        return new UberdustParserFragment();
//    }

    String date;
    String title;
    String content;
    String posturl;
    
    String mUsername;
    String mToken;
    
    String json_html = "";
    String old_json;

    private SharedPreferences settings;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> postList;
    // creating new HashMap
    HashMap<String, String> map;

    // url to make request
    String j_url;
    
    List<String> urls = new ArrayList<String>();

    // JSON Node names
    private static final String TAG_TITLE = "title";
    private static final String TAG_URL = "url";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_DATE = "published_on";
    private static final String TAG_POSTS = "posts";

    // contacts JSONArray
    JSONArray posts = null;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setSubtitle("Alpha feature");
        restorePreferences();
        
        j_url = "http://" + mUsername +".github.com" + "/json/";
        
        if(old_json.equals(""))
        	new HtmlToJson().execute(j_url);
        else
        	new ParsePostsData().execute(old_json);
        	new HtmlToJson().execute(j_url);
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
        }
    }

    private class ParsePostsData extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {            
            
        	String json_str = params[0];
            JSONObject json = null;
            
            // Hashmap for ListView
            postList = new ArrayList<HashMap<String, String>>();

            // Creating JSON Parser instance
            JSONParser jParser = new JSONParser();

            // getting JSON string from URL
//            JSONObject json = new JSONObject();
            
            try {
            	json = new JSONObject(json_str);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

            try {
                // Getting Array of Readings
                posts = json.getJSONArray(TAG_POSTS);

                // looping through All Contacts
                for(int i = 0; i < posts.length(); i++){
                    JSONObject r = posts.getJSONObject(i);

                    // Storing each json item in variable
                    title = r.getString(TAG_TITLE);
                    posturl = r.getString(TAG_URL);
                    date = r.getString(TAG_DATE);
                    
                    urls.add(posturl);
                    
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
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            /**
             * Updating parsed JSON data into ListView
             * */
             adapter = new SimpleAdapter(PostsListActivity.this, postList,
                    R.layout.activity_posts_list,
                    new String[] { TAG_DATE, TAG_TITLE }, new int[] {
                    R.id.pdate, R.id.ptitle });

            setListAdapter(adapter);
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
    
    protected void onListItemClick(ListView l, View v, int position, long id){
    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls.get(position)));
    	startActivity(browserIntent);
    }
}